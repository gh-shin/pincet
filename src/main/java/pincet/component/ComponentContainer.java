/*
 * Copyright (c) 2016. Epozen co. Author Steve Shin.
 */

package pincet.component;

import com.jws.framework.annotation.Component;
import com.jws.framework.exception.ContainerInitializeException;
import lombok.Getter;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * {@inheritDoc}
 */
public class ComponentContainer implements Container<SingletonProxyFactory> {

  private static final Logger log = LoggerFactory.getLogger(ComponentContainer.class);
  /**
   * 각 객체들 간 DI 정의
   */
  @Getter
  private Map<String, DependencyInjector> injectorFactory;
  /**
   * 프록시 객체들의 lifecycle 정책
   */
  @Getter
  private ComponentPolicyFactory policyFactory;
  /**
   * 프록시 객체들을 저장
   */
  private SingletonProxyFactory proxyFactory;
  private boolean valid = false;

  @Override
  public CoreExtensions getExtentionType() {
    return CoreExtensions.DEFAULT;
  }

  @Override
  public SingletonProxyFactory getFactory() {
    return proxyFactory;
  }

  @Override
  public synchronized ComponentContainer initializeToScan(String scanPackage) throws ContainerInitializeException {
    try {
      Set<Class<?>> componentClasses = null;

      if (scanPackage.equals("com.jws.framework")) {
        log.warn("com.jws.framework package will scan automatically.");
      } else {
        log.info("package {} scanning start.", scanPackage);
        Reflections ref = new Reflections(scanPackage);
        componentClasses = ref.getTypesAnnotatedWith(Component.class);
      }

      Set<Class<?>> moduleClasses = new Reflections("com.jws.framework").getTypesAnnotatedWith(Component.class);

      policyFactory = new ComponentPolicyFactory();

      //default module append
      if (componentClasses != null) {
        moduleClasses.addAll(componentClasses);
      }

      moduleClasses.forEach(e -> {
        Component managerAnno = e.getDeclaredAnnotation(Component.class);
        policyFactory.put(e, new PolicyDefine(managerAnno.name(), e));
      });

      ComponentDependencyResolver resolver = new ComponentDependencyResolver();
      resolver.resolve(policyFactory);

      proxyFactory = new SingletonProxyFactory();

      injectorFactory = this._inject(resolver.getValues());


      valid = true;
      return this;
    } catch (Throwable e) {
      throw new ContainerInitializeException(e);
    }
  }

  @Override
  public boolean valid() {
    return valid;
  }

  /**
   * 실제 D/I 수행
   *
   * @param idValues - DI 정의들
   * @return DI 수행 후 완료된 DI정의 객체를 반환
   */
  private Map<String, DependencyInjector> _inject(Set<ComponentDependencyResolver.DependencyIdMaps> idValues) {

    Map<String, DependencyInjector> injectorMap = new HashMap<>(idValues.size());

    idValues.forEach(e -> {

      Set<String> singleIds = e.getSingleIds();
      Set<String> instantIds = e.getInstantIds();

      if (singleIds == null)
        //noinspection unchecked
        singleIds = Collections.EMPTY_SET;
      if (instantIds == null)
        //noinspection unchecked
        instantIds = Collections.EMPTY_SET;

      DependencyInjector injector = new DependencyInjector();
      injector.setId(e.getId());
      injector.setClz(e.getClz());
      injector.setInstantIds(instantIds);

      if (e.getClz().getDeclaredAnnotation(Component.class).singleton()) {
        Object result = injector.createSingleton(proxyFactory, policyFactory);
        String[] others = e.getClz().getDeclaredAnnotation(Component.class).other();
        if (others.length > 0) {
          injector.setOthers(others);
          for (int i = 0; i < others.length; i++) {
            DependencyInjector subInjector = new DependencyInjector();
            subInjector.setId(others[i]);
            subInjector.setClz(e.getClz());
            subInjector.setInstantIds(instantIds);
            Object subResult = subInjector.createSingleton(proxyFactory, policyFactory);
            proxyFactory.put(others[i], subResult);
          }
        }
        proxyFactory.put(e.getId(), result);
        log.debug("Singleton object created {}", result.getClass().getName());
      } else {
        injector.createInstant(policyFactory);
      }

      injectorMap.put(e.getId(), injector);

    });
    return injectorMap;
  }
}
