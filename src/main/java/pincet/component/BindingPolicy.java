/*
 * Copyright (c) 2016. Epozen co. Author Steve Shin.
 */

package pincet.component;

/**
 * Component 객체 정의 시 사용 되는 lifecycle 정책
 *
 * @author Shingh on 2016-06-16.
 */
public enum BindingPolicy {
  Singleton, Instant;

  public static BindingPolicy is(boolean singleton) {
    return singleton ? Singleton : Instant;
  }
}
