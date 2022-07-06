package dev.gump.worm.field;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Relationship {
    /**
     * What will do when this relation be updated
     */
    RelationshipEvent onUpdate() default RelationshipEvent.CASCADE;
    /**
     * What will do when this relation be deleted
     */
    RelationshipEvent onDelete() default RelationshipEvent.CASCADE;
}
