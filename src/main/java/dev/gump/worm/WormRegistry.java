package dev.gump.worm;

import dev.gump.worm.entity.Entity;
import dev.gump.worm.entity.EntityMeta;
import dev.gump.worm.field.Context;
import dev.gump.worm.field.SequenceType;
import dev.gump.worm.field.WormField;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.*;

public final class WormRegistry {

    private final Map<Class<? extends Entity>, EntityMeta> tableMap = new HashMap<>();
    private final List<EntityMeta> tables = new ArrayList<>();

    WormRegistry() {}

    /**
     * Register a table in Worm
     *
     * @param tableClass a class that extends Entity
     */
    public void registerTable(Class<? extends Entity> tableClass) {
        registerTable(tableClass.getSimpleName(), tableClass);
    }

    /**
     * Register a table in Worm
     *
     * @param tableName a table name in database
     * @param tableClass a class that extends Entity
     */
    public void registerTable(String tableName, Class<? extends Entity> tableClass) throws WormException {
        List<WormField> columns = new ArrayList<>();
        WormField autoIncrementColumn = null;

        List<Context> contexts = new ArrayList<>(Arrays.asList(tableClass.getDeclaredAnnotationsByType(Context.class)));

        for (Field field : tableClass.getDeclaredFields()) {
            field.setAccessible(true);
            dev.gump.worm.field.Field wormField = field.getAnnotation(dev.gump.worm.field.Field.class);
            if (wormField == null) continue;

            WormField column = new WormField(field);

            if (column.isUnique() && column.isNullable())
                throw new WormException("Primary Key columns cannot be nullable! Column: " + column.getName() + " | Table " + tableName);

            if (column.getIncrement() == SequenceType.AUTO_INCREMENT && !column.isUnique())
                throw new WormException("Auto Increment is only allowed in a Primary Key column! Column: " + column.getName() + " | Table " + tableName);

            if (column.getIncrement() == SequenceType.AUTO_INCREMENT && autoIncrementColumn != null)
                throw new WormException("Only one Auto Increment column is allowed! Column: " + column.getName() + " | Table " + tableName);

            if (column.getIncrement() == SequenceType.AUTO_INCREMENT) {
                autoIncrementColumn = column;
                continue;
            }

            columns.add(column);
        }

        EntityMeta tableMeta = new EntityMeta(tableClass, tableName, columns, contexts);

        this.tableMap.put(tableClass, tableMeta);
        this.tables.add(tableMeta);
    }

    /**
     * Get the table meta
     *
     * @param tableClass a class that extends Entity
     */
    public EntityMeta getTableMeta(Class<? extends Entity> tableClass) {
        return Objects.requireNonNull(this.tableMap.get(tableClass), "Table Class " + tableClass.getName() + " was not registered!");
    }

    List<EntityMeta> getTables() {
        return this.tables;
    }

}
