/*
 * Copyright (c) 2016. Epozen co. Author Steve Shin.
 */

package pincet.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Shingh on 2016-05-10.
 * <p>
 * ReservedTarget이 정의된 메서드에서 사용할 인자 필드.
 * 메서드 내의 인자명과 이 어노테이션이 붙은 필드의 명이 일치되어야 함
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface ReservedArguments {
  String field();
}
