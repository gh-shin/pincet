/*
 * Copyright (c) 2016. Epozen co. Author Steve Shin.
 */

package pincet.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Shingh on 2016-05-09.
 * ExecutionTarget 내의 메서드에 적용.
 * 스케줄러에서 실제 수행 될 메서드.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ReservedTarget {
  String expression();

  String id() default "";
}
