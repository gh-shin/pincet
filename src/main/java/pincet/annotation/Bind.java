/*
 * Copyright (c) 2016. Epozen co. Author Steve Shin.
 */

package pincet.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author Shingh on 2016-06-16.
 * Object DI 관련 정의
 */
@Target(FIELD)
@Retention(RUNTIME)
public @interface Bind {
  /**
   * Bind 될 Component 명
   * 기본일 경우 해당 필드명을 기준으로 주입
   *
   * @return
   */
  String name() default "";
}
