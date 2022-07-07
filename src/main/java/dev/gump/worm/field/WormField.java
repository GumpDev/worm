package dev.gump.worm.field;

import dev.gump.worm.Worm;
import dev.gump.worm.WormException;
import dev.gump.worm.WormUtils;
import dev.gump.worm.entity.Entity;
import dev.gump.worm.entity.EntityMeta;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

public class WormField {
    private final FieldType type;
    private final String name;
    private final String field_name;
    private int length;
    private SequenceType increment = SequenceType.NONE;
    private final boolean unique;
    private final String defaultValue;
    private final boolean nullable;
    private Class<? extends Entity> fieldClass_relational;

    private RelationshipEvent onUpdate = RelationshipEvent.CASCADE;
    private RelationshipEvent onDelete = RelationshipEvent.CASCADE;

    public WormField(Field field) {
        int length1;
        dev.gump.worm.field.Field fieldAnnotation = field.getAnnotation(dev.gump.worm.field.Field.class);
        this.field_name = field.getName();
        if(field.getType().getSuperclass() == Entity.class) {
            this.fieldClass_relational = (Class<? extends Entity>) field.getType();
            this.type = FieldType.ENTITY;
        }else{
            if (fieldAnnotation.type() != FieldType.AUTO)
                this.type = fieldAnnotation.type();
            else
                this.type = FieldType.findType(field.getType());
        }

        if(!Objects.equals(fieldAnnotation.name(), ""))
            this.name = fieldAnnotation.name();
        else
            this.name = field.getName();

        if(fieldAnnotation.autoGenerate()){
            if (field.getType().equals(UUID.class)) {
                increment = SequenceType.RANDOM_UUID;
                this.length = 36;
            }else if(field.getType().equals(Integer.class))
                increment = SequenceType.AUTO_INCREMENT;
            else if(field.getType().equals(String.class)) {
                increment = SequenceType.RANDOM_STRING;
                this.length = 32;
            }else if(field.getType().equals(Date.class) ||
                    field.getType().equals(Long.class) ||
                    field.getType().equals(LocalDate.class) ||
                    field.getType().equals(LocalDateTime.class) ||
                    field.getType().equals(ZonedDateTime.class)
                )
                increment = SequenceType.NOW_DATE;
        }else{
            length1 = fieldAnnotation.length();
            if (fieldAnnotation.length() == 0 && type == FieldType.VARCHAR)
                length1 = 255;
            this.length = length1;
        }

        this.unique = fieldAnnotation.unique();
        this.defaultValue = fieldAnnotation.defaultValue();
        this.nullable = fieldAnnotation.nullable();


        Relationship relationshipAnnotation = field.getAnnotation(Relationship.class);
        if(relationshipAnnotation != null){
            onUpdate = relationshipAnnotation.onUpdate();
            onDelete = relationshipAnnotation.onDelete();
        }
    }

    public String getEntityId(Object obj) throws IllegalAccessException, NoSuchFieldException {
        if(type != FieldType.ENTITY) return "";
        EntityMeta cMeta = Worm.getRegistry().getTableMeta(fieldClass_relational);
        if(cMeta.getUniqueKeyColumns().size() != 1)
            throw new WormException("Relational Worm tables need to have only one unique keys");
        WormField key = cMeta.getUniqueKeyColumns().toArray(WormField[]::new)[0];

        Field field = obj.getClass().getDeclaredField(key.getFieldName());
        field.setAccessible(true);
        Object value = field.get(obj);

        if(key.getType().isNeedAsps())
            return "'"+WormUtils.escapeToSql(value.toString())+"'";
        else
            return value.toString();
    }

    public String getSqlCreation(){
        if(type == FieldType.ENTITY && fieldClass_relational != null){
            EntityMeta cMeta = Worm.getRegistry().getTableMeta(fieldClass_relational);
            if(cMeta.getUniqueKeyColumns().size() != 1)
                throw new WormException("Relational Worm tables need to have only one unique keys");
            WormField key = cMeta.getUniqueKeyColumns().toArray(WormField[]::new)[0];
            StringBuilder sql = new StringBuilder(key.getSqlCreation());
            sql.append(", CONSTRAINT FK_").append(getName()).append("_").append(cMeta.getName()).append(" FOREIGN KEY (").append(getName()).append(") REFERENCES ")
                    .append(cMeta.getName()).append(" (").append(key.getName()).append(")");
            if(onDelete != null)
                sql.append(" ON DELETE ").append(onDelete.getValue());
            if(onUpdate != null)
                sql.append(" ON UPDATE ").append(onUpdate.getValue());
            return sql.toString();
        }

        StringBuilder sqlSpec = new StringBuilder(type.getValue());
        if (length > 0)
            sqlSpec.append('(').append(length).append(')');
        if (!nullable)
            sqlSpec.append(" NOT NULL");
        if (increment == SequenceType.AUTO_INCREMENT)
            sqlSpec.append(" AUTO_INCREMENT");
        if(increment == SequenceType.NOW_DATE)
            sqlSpec.append(" DEFAULT now()");
        else if (!Objects.equals(defaultValue, "")) {
            if(getType().isNeedAsps())
                sqlSpec.append(" DEFAULT '").append(WormUtils.escapeToSql(defaultValue)).append('\'');
            else
                sqlSpec.append(" DEFAULT ").append(defaultValue);
        }
        return sqlSpec.toString();
    }

    public FieldType getType() {
        return type;
    }

    public String getFieldName() {
        return field_name;
    }

    public String getName() {
        return name;
    }

    public int getLength() {
        return length;
    }

    public SequenceType getIncrement() {
        return increment;
    }

    public boolean isUnique() {
        return unique;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public boolean isNullable() {
        return nullable;
    }
}
