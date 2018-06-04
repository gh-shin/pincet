package pincet.annotation;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author shingh on 2017-02-14.
 */
@Target({FIELD, ANNOTATION_TYPE})
@Retention(RUNTIME)
@Inherited
public @interface Shared {
  String id() default "";

  boolean lookup() default false;

  int maxLength() default Integer.MAX_VALUE;

  Type type() default Type.Include;
  //Include -> included in sharedMap. Separate -> another map by id

  enum Type {
    Include, Separate
  }
}
