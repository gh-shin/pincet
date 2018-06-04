package pincet.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author shingh on 2017-02-14.
 */
@Target(FIELD)
@Retention(RUNTIME)
@Shared(lookup = true)
public @interface NodeConfig {
}
