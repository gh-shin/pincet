/*
 * Copyright (c) 2016. Epozen co. Author Steve Shin.
 */

package pincet;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target(TYPE)
@Retention(RUNTIME)
public @interface PincetObject {
  boolean methodTracing() default false;

  String name() default "";

  String[] others() default {};

  boolean singleton() default true;

}
