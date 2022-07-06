package dev.gump.worm.entity;

import dev.gump.worm.Worm;
import dev.gump.worm.WormException;
import dev.gump.worm.WormQuery;
import dev.gump.worm.WormUtils;
import dev.gump.worm.field.FieldType;
import dev.gump.worm.field.WormField;
import dev.gump.worm.typeadapter.WormTypeAdapter;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class EntityLoader {
    public static Object load(Class<? extends Entity> entityClass, ResultSet set, EntityMeta meta) throws NoSuchFieldException, IllegalAccessException, SQLException, InstantiationException {
        Object instance = entityClass.newInstance();
        for (WormField column : meta.getColumns()) {
            Field field = instance.getClass().getDeclaredField(column.getFieldName());
            field.setAccessible(true);
            try {
                if (set.getObject(column.getName()) == null) {
                    field.set(instance, null);
                    continue;
                }
            }catch (SQLException e){
                continue;
            }

            Class<?> type = field.getType();

            // Java Primitives
            if (type == boolean.class || type == Boolean.class)
                field.set(instance, set.getBoolean(column.getName()));
            else if (type == char.class || type == Character.class)
                field.set(instance, set.getString(column.getName()).charAt(0));
            else if (type == byte.class || type == Byte.class)
                field.set(instance, set.getByte(column.getName()));
            else if (type == short.class || type == Short.class)
                field.set(instance, set.getShort(column.getName()));
            else if (type == int.class || type == Integer.class)
                field.set(instance, set.getInt(column.getName()));
            else if (type == long.class || type == Long.class)
                field.set(instance, set.getLong(column.getName()));
            else if (type == float.class || type == Float.class)
                field.set(instance, set.getFloat(column.getName()));
            else if (type == double.class || type == Double.class)
                field.set(instance, set.getDouble(column.getName()));
            else if (type == LocalDateTime.class || type == ZonedDateTime.class || type == Date.class)
                field.set(instance, set.getDate(column.getName()));
            else if (type == String.class)
                field.set(instance, set.getString(column.getName()));
            else {
                if(field.getType().getSuperclass() == Entity.class){
                    try {
                        field.set(instance, Worm.of((Class<? extends Entity>) field.getType()).findUnique(set.getString(column.getName())).get());
                    } catch (InterruptedException | ExecutionException e) {
                        throw new RuntimeException(e);
                    }
                }else {
                    @SuppressWarnings("rawtypes")
                    WormTypeAdapter typeAdapter = Worm.getTypeAdapterRegistry().getTypeAdapter(type);
                    if (typeAdapter == null)
                        throw new WormException(type.getName() + " type is not supported!");

                    Object result = typeAdapter.fromDatabase(set, column.getName());
                    field.set(instance, result);
                }
            }
        }
        return instance;
    }

    public static HashMap<String, String> save(Object obj, EntityMeta meta) throws NoSuchFieldException, IllegalAccessException {
        HashMap<String, String> fields = new HashMap<>();
        for (WormField column : meta.getColumns()) {
            if(column.isUnique()) continue;
            Field field = obj.getClass().getDeclaredField(column.getFieldName());
            field.setAccessible(true);
            Object value = field.get(obj);

            if(column.getType() == FieldType.ENTITY){
                if(value != null)
                    fields.put(column.getName(), column.getEntityId(value));
                else
                    fields.put(column.getName(),"DEFAULT");
            }else if (value != null) {
                String stringValue;

                WormTypeAdapter typeAdapter = Worm.getTypeAdapterRegistry().getTypeAdapter(field.getType());
                if (typeAdapter != null)
                    stringValue = typeAdapter.toDatabase(value);
                else
                    stringValue = value.toString();

                if(column.getType().isNeedAsps())
                    fields.put(column.getName(), new StringBuilder().append('\'').append(WormUtils.escapeToSql(stringValue)).append('\'').toString());
                else
                    fields.put(column.getName(), stringValue);
            } else {
                fields.put(column.getName(), "DEFAULT");
            }
        }
        return fields;
    }
}
