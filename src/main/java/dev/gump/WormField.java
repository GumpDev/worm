package dev.gump;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface WormField {
	public String sqlType() default "TEXT";
	public int length() default 0;
	public boolean autoIncrement() default false;
	public boolean idColumn() default false;
	public String defaultValue() default "";
}
