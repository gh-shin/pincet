/*
 * Copyright (c) 2016. Epozen co. Author Steve Shin.
 */

package pincet.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author Shingh on 2016-07-18.
 */
@Target({TYPE, METHOD})
@Retention(RUNTIME)
public @interface TraceDefine {

  boolean argumentCheck() default false;

  boolean elapseTimeCheck() default false;

  boolean returnTypeCheck() default false;
}