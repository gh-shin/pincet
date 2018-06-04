/*
 * Copyright (c) 2016. Epozen co. Author Steve Shin.
 */

package pincet.annotation;

import com.jws.framework.components.SingletonProxyFactory;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author Shingh on 2016-04-26.
 * <p>
 * ArcContainer 클래스에 등록 될 클래스에 삽입
 * name에 정의된 이름을 Key로 하여 ArcComponentContainer 내에 등록 되어 관리된다.
 * @see SingletonProxyFactory
 */
@Target(TYPE)
@Retention(RUNTIME)
public @interface Component {
  /**
   * 설정 시 해당 오브젝트의 메서드가 수행 될 때 수행 시간, 입력값, 리턴 타입을 체크하여 로깅
   *
   * @return
   */
//  TraceDefine methodTracing() default @TraceDefine;
  boolean methodTracing() default false;

  /**
   * ID로 사용될 Object의 이름
   *
   * @return
   */
  String name() default "";

  String[] other() default {};

  /**
   * Object 정책을 singleton으로 설정할 것인지 여부
   *
   * @return
   */
  boolean singleton() default true;


}
