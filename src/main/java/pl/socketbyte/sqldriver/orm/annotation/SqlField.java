package pl.socketbyte.sqldriver.orm.annotation;

import pl.socketbyte.sqldriver.query.SqlDataType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to mark custom characteristics of
 * ORM fields like custom record name
 * or custom sql data type.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface SqlField {
    String name() default "";

    @SuppressWarnings("deprecation")
    SqlDataType type() default SqlDataType.AUTO_DETECT;
}
