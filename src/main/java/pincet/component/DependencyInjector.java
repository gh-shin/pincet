/*
 * Copyright (c) 2016. Epozen co. Author Steve Shin.
 */

package pincet.component;

import lombok.Getter;
import lombok.Setter;
import net.sf.cglib.proxy.CallbackFilter;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pincet.Pincet;
import pincet.annotation.Component;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static pincet.PincetArgs.STAT_METHOD;


/**
 * 객체 정의를 받아 정책에 맞는 프록시를 생성하고 factory에 저장
 *
 * @author Shingh on 2016-07-18.
 */
public class DependencyInjector {
  private static final Logger log = LoggerFactory.getLogger(DependencyInjector.class);
  @Setter
  @Getter
  private Class<?> clz;
  @Getter
  @Setter
  private String id;
  @Setter
  private Set<String> instantIds;
  @Getter
  private Map<String, FieldMapping> mappingInfo;
  @Getter
  @Setter
  private String[] others;

  DependencyInjector() {
    this.mappingInfo = new HashMap<>();
  }

  public void createInstant(ComponentPolicyFactory policies) {
    _buildInjectorInfo(policies);
  }

  public Object createSingleton(ComponentFactory factory, ComponentPolicyFactory policies) {
    //TODO 해당 클래스에서 사용하는 필드 번호에 객체를 직접 삽입하고, 팩토리는 제거
    Object componentProxy = factory.get(id) != null ? factory.get(id) : _createProxy(null, new MethodExecutionProxy(factory, instantIds, clz.getDeclaredAnnotation(Component.class).methodTracing()));
    _buildInjectorInfo(policies);
    return componentProxy;
  }

  private void _buildInjectorInfo(ComponentPolicyFactory policies) {
    BindingPolicy policy = policies.getPolicy(id);
    PolicyDefine policyDefine = policies.getDefine(policy, id);
    Stream.of(policyDefine.getClz().getDeclaredFields())
        .filter(ComponentDependencyResolver::isBindableField)
        .forEach(f -> {
          FieldMapping mapping = new FieldMapping();
          mapping.className = f.getType().getTypeName();
          mapping.name = f.getName();
          mapping.type = policy.toString();
          mapping.others = f.getType().getAnnotation(Component.class).other();
          String id = f.getType().getAnnotation(Component.class).name();
          mappingInfo.put(id, mapping);
        });
  }

  private Object _createProxy(CallbackFilter filter, MethodInterceptor... interceptor) {
    Enhancer result = new Enhancer();
//        List<Class<?>> interfaces = Lists.newArrayList();
//        _resolveInterfaces(interfaces, clz);
    Class<?>[] refClasses = clz.getClasses();
    Class<?>[] refInterfaces = Stream.of(refClasses).filter(Class::isInterface).toArray(Class<?>[]::new);

    result.setInterfaces(refInterfaces);
    //result.setSuperclass(!clz.getSuperclass().equals(Object.class) && !clz.getSuperclass().isInterface() ? clz.getSuperclass() : clz);
    result.setSuperclass(clz);
    result.setCallbacks(interceptor);

    if (filter != null && interceptor.length > 1) {
      result.setCallbackFilter(filter);
    }
    return result.create();
  }

  private void _resolveInterfaces(List<Class<?>> appender, Class<?> clz) {
    if (!clz.equals(Object.class)) {
      if (clz.getSuperclass().isInterface()) {
        appender.add(clz.getSuperclass());
      } else {
        _resolveInterfaces(appender, clz.getSuperclass());
      }
    }
  }

  public static class FieldMapping {
    @Getter
    String className;
    @Getter
    String name;
    @Getter
    String[] others;
    @Getter
    String type;
  }

  private static class MethodExecutionProxy implements MethodInterceptor {
    private ComponentFactory ComponentFactory;
    private Set<String> instants;
    private boolean methodTrace;

    MethodExecutionProxy(ComponentFactory ComponentFactory, Set<String> instants, boolean methodTrace) {
      this.ComponentFactory = ComponentFactory;
      this.instants = instants;
      this.methodTrace = methodTrace;
    }

    MethodExecutionProxy(ComponentFactory ComponentFactory, Set<String> instants) {
      this(ComponentFactory, instants, false);
    }

    @Override
    public Object intercept(Object proxyObject, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
      if (method.getName().equals("toString") || method.getName().equals("hashCode") || method.getName().equals("equals")) {
        return methodProxy.invokeSuper(proxyObject, null);
      }
      Class<?> targetClass = method.getDeclaringClass();
      if (targetClass.getSuperclass() != null) {
        Field[] fields = method.getDeclaringClass().getDeclaredFields();
        for (Field f : fields) {
          if (ComponentDependencyResolver.isBindableField(f)) {
            f.setAccessible(true);
            try {
              String bindId = ComponentDependencyResolver.getBindName(f);
              if (instants.contains(bindId)) {
                f.set(proxyObject, f.getType().newInstance());
              } else {
                if (f.get(proxyObject) == null) {
                  Object singletonProxy = ComponentFactory.get(bindId);
                  f.set(proxyObject, singletonProxy);
                }
              }
            } catch (IllegalAccessException | InstantiationException e) {
              log.error("instant object create failed because of Exception.", e);
            }
            f.setAccessible(false);
          }
        }
      }
      long start = -1;
      boolean isTrace = System.getProperty(STAT_METHOD.getKey()) != null && methodTrace;
      if (isTrace) {
        start = System.currentTimeMillis();
      }
      Object result;
      try {
        result = methodProxy.invokeSuper(proxyObject, args);
      } catch (RuntimeException e) {
        log.error("Target method invoke failed by RuntimeException.", e);
        result = null;
      }
      if (isTrace) {
        Pincet.logger().debug("'{}#{}' method invoke complete. {} elapsed.", targetClass.getName(), method.getName(), (System.currentTimeMillis() - start));
      }
      return result;
    }
  }
}
