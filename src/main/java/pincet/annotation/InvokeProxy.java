/*
 * Copyright (c) 2016. Epozen co. Author Steve Shin.
 */

package pincet.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author Shingh on 2016-05-12.
 * <p>
 * 특정 클래스의 특정 메서드 수행 시 전/후처리로 사용할 listener 정의
 * @see com.epozen.framework.annotation.InvokeProxy.InvokeTiming
 */
@Target(METHOD)
@Retention(RUNTIME)
public @interface InvokeProxy {
  int order() default 0;

  Class<?> targetClass();

  String[] targetMethodName() default "*";

  InvokeTiming timing();

  public enum InvokeTiming {
    Before, After
  }
}
