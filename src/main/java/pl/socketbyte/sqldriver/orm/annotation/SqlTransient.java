package pl.socketbyte.sqldriver.orm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to mark fields that will not be
 * a part of the ORM object i.e. will not
 * be saved/recognized by SQL driver
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface SqlTransient {
}
