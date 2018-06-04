package pincet.component;

import lombok.extern.slf4j.Slf4j;
import pincet.annotation.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public final class ComponentFactory {
  private static final ConcurrentHashMap<String, Object> CONTAINER = new ConcurrentHashMap<>();

  public Object get(Object id) {
    if (id instanceof Class)
      return this.getForClass((Class<?>) id);
    return CONTAINER.get(id);
  }

  @SuppressWarnings("unchecked")
  public <T> T get(final Object id, final Class<T> clz) {
    Object proxy = get(id);
    if (proxy == null) {
      log.info("{} is not singleton component. factory return to be created object.", id);
      try {
        return clz.newInstance();
      } catch (InstantiationException | IllegalAccessException e) {
        log.error("", e);
        return null;
      }
    } else {
      Class<?> proxySuperClass = proxy.getClass().getSuperclass();
      if (clz.equals(proxySuperClass)) {
        return (T) proxy;
      } else {
        return null;
      }
    }
  }

  @SuppressWarnings("unchecked")
  public <T> T getForClass(final Class<T> clz) {
    T result = null;
    for (Map.Entry<String, Object> ent : CONTAINER.entrySet()) {
      if (clz.isAssignableFrom(ent.getValue().getClass())) {
        result = (T) ent.getValue();
        break;
      }
    }
    return result;
  }

  public void put(Object object) {
    if (object.getClass().isAnnotationPresent(Component.class)) {
      this.put(object.getClass().getAnnotation(Component.class).name(), object);
    } else {
      IllegalArgumentException e = new IllegalArgumentException("ManagerContainer can be consumed by presented ArcManager Annotation class.");
      log.error("ManagerContainer can be consumed by presented ArcManager Annotation class.{}", e);
      throw e;
    }
  }

  public Object put(String id, Object value) {
    CONTAINER.put(id, value);
    return value;
  }
}
