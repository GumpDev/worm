package dev.gump.worm;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface WormField {
	String sqlType();
	String sqlName() default "";
	int length() default 0;
	boolean autoIncrement() default false;
	boolean primaryKey() default false;
	String defaultValue() default "";
	boolean nullable() default false;
}
