package dev.gump.worm.field;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Repeatable(Contexts.class)
public @interface Context {
    /**
     * context name
     */
    String name();
    /**
     * database fields that will be ignored on query
     */
    String[] ignoredFields() default "";
}
