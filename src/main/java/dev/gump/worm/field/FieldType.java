package dev.gump.worm.field;

import dev.gump.worm.entity.Entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.*;

public enum FieldType {
    AUTO("AUTO", List.of()),
    TEXT("TEXT", List.of(), true),
    VARCHAR("VARCHAR", List.of(String.class, UUID.class), true),
    INT("INT", List.of(Integer.class, int.class)),
    FLOAT("FLOAT", List.of(Float.class)),
    DOUBLE("DOUBLE", List.of(Double.class)),
    LONG("LONG", List.of(Long.class)),
    CHAR("CHAR", List.of(Character.class), true),
    DATE("DATE", List.of(LocalDate.class, Date.class), true),
    DATETIME("DATETIME", List.of(LocalDateTime.class, ZonedDateTime.class), true),
    TIME("TIME", List.of(), true),
    TIMESTAMP("TIMESTAMP", List.of(), true),
    BOOL("BOOL", List.of(Boolean.class)),
    ENTITY("ENTITY",List.of(Entity.class), true);
    private final String value;
    private final List<Class<?>> classes;
    private final boolean needAsps;
    FieldType(String value, List<Class<?>> classes){
        this.value = value;
        this.classes = classes;
        this.needAsps = false;
    }
    FieldType(String value, List<Class<?>> classes, boolean needAsps){
        this.value = value;
        this.classes = classes;
        this.needAsps = needAsps;
    }

    public String getValue() {
        return value;
    }

    public boolean hasClasses(Class<?> type) {
        for(Class<?> c : classes){
            if(c == type || type.isAssignableFrom(c))
                return true;
        }
        return false;
    }

    public static FieldType findType(Class<?> type){
        for(FieldType t : FieldType.values()){
            if(t.hasClasses(type))
                return t;
        }
        return FieldType.TEXT;
    }

    public boolean isNeedAsps() {
        return needAsps;
    }
}
