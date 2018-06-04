/*
 * Copyright (c) 2016. Epozen co. Author Steve Shin.
 */

package pincet.component;

import com.google.common.base.Objects;
import pincet.annotation.Component;
import pincet.exception.UnMatchedInjectionDefine;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Component의 lifecycle 정책을 담는 객체
 *
 * @author Shingh on 2016-06-16.
 */
public class ComponentPolicyFactory extends EnumMap<BindingPolicy, Set<PolicyDefine>> {

  private final Set<String> ids;

  ComponentPolicyFactory() {
    super(BindingPolicy.class);
    super.put(BindingPolicy.Singleton, new HashSet<>());
    super.put(BindingPolicy.Instant, new HashSet<>());
    this.ids = new HashSet<>();
  }

  public boolean contains(final PolicyDefine obj) {
    return super.get(BindingPolicy.Instant).contains(obj) || super.get(BindingPolicy.Singleton).contains(obj);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;
    ComponentPolicyFactory that = (ComponentPolicyFactory) o;
    return Objects.equal(ids, that.ids);
  }

  public PolicyDefine getDefine(BindingPolicy policy, String id) {
    Set<PolicyDefine> defines = get(policy);
    return defines.stream().filter(e -> e.getObjectName().equals(id)).limit(1).iterator().next();
  }

  public Set<String> getIds() {
    return ids;
  }

  public BindingPolicy getPolicy(String id) {
    Set<PolicyDefine> instants = super.get(BindingPolicy.Instant);
    long inst = instants.stream().filter(e -> e.getObjectName().equals(id)).count() > 0 ? 1 : 0;
    if (inst == 1) return BindingPolicy.Instant;
    Set<PolicyDefine> singles = super.get(BindingPolicy.Singleton);
    inst = singles.stream().filter(e -> e.getObjectName().equals(id)).count() > 0 ? 1 : 0;
    if (inst == 0)
      throw new UnMatchedInjectionDefine(id + " is not present in policy defines.");
    return BindingPolicy.Singleton;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(super.hashCode(), ids);
  }

  public void put(Class<?> clz, PolicyDefine policyDefine) {
    if (clz.getAnnotation(Component.class).singleton()) {
      get(BindingPolicy.Singleton).add(policyDefine);
    } else {
      get(BindingPolicy.Instant).add(policyDefine);
    }
    this.ids.add(policyDefine.getObjectName());
  }
}
