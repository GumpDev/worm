package dev.gump.worm.field;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Field {
	/**
	 * field type on database, default is AUTO
	 */
	FieldType type() default FieldType.AUTO;
	/**
	 * field name on database, default is field name on Java
	 */
	String name() default "";
	/**
	 * field length on database
	 */
	int length() default 0;
	/**
	 * field auto generator
	 */
	boolean autoGenerate() default false;
	/**
	 * field is unique in database
	 */
	boolean unique() default false;
	/**
	 * field default value
	 */
	String defaultValue() default "";
	/**
	 * field can be null
	 */
	boolean nullable() default false;
}
