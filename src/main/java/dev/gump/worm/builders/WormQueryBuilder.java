package dev.gump.worm.builders;

import dev.gump.worm.Worm;
import dev.gump.worm.WormUtils;
import dev.gump.worm.entity.Entity;
import dev.gump.worm.entity.EntityMeta;
import dev.gump.worm.field.FieldType;
import dev.gump.worm.field.SequenceType;
import dev.gump.worm.field.WormField;
import dev.gump.worm.typeadapter.WormTypeAdapter;

import java.lang.reflect.Field;
import java.util.*;

public class WormQueryBuilder {
    public static String createTable(EntityMeta meta){
        StringBuilder builder = new StringBuilder("CREATE TABLE IF NOT EXISTS ").append(meta.getName()).append(" (");

        boolean first = true;
        for (WormField column : meta.getColumns()) {
            builder.append(first ? "" : ", ").append('`').append(column.getName()).append("` ").append(column.getSqlCreation());
            first = false;
        }

        if (meta.getUniqueKeyColumns().size() > 0) {
            builder.append(", PRIMARY KEY(");
            first = true;
            for (WormField column : meta.getUniqueKeyColumns()) {
                builder.append(first ? "" : ", ").append('`').append(column.getName()).append('`');
                first = false;
            }
            builder.append(')');
        }

        builder.append(")");

        //Add Relational and Lists

        return builder.toString();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static List<String> insert(EntityMeta tableMeta, Entity table) throws NoSuchFieldException, IllegalAccessException {
        List<String> result = new ArrayList<>();
        result.add("");
        StringBuilder builder = new StringBuilder("INSERT INTO `").append(tableMeta.getName()).append("`(");
        boolean first = true;
        for (WormField field : tableMeta.getColumns()) {
            builder.append(first ? "" : ", ").append('`').append(field.getName()).append("`");

            first = false;
        }
        builder.append(") VALUES (");

        first = true;
        for (WormField column : tableMeta.getColumns()) {
            Field field = table.getClass().getDeclaredField(column.getName());
            field.setAccessible(true);
            Object value = field.get(table);

            builder.append(first ? "" : ", ");

            if(column.getType() == FieldType.ENTITY){
                if(value != null)
                    builder.append(column.getEntityId(value));
                else
                    builder.append("NULL");
            }
            else if(column.getIncrement() == SequenceType.AUTO_INCREMENT){
                builder.append("DEFAULT");
            }
            else if (value != null || column.getIncrement() == SequenceType.RANDOM_STRING || column.getIncrement() == SequenceType.RANDOM_UUID) {
                String stringValue;

                if(column.getIncrement() == SequenceType.RANDOM_STRING){
                    UUID randomUUID = UUID.randomUUID();
                    stringValue = randomUUID.toString().replaceAll("_", "").substring(0,column.getLength() == 0 ? 32 : column.getLength());
                    result.add(stringValue);
                }else if(column.getIncrement() == SequenceType.RANDOM_UUID){
                    stringValue = UUID.randomUUID().toString();
                    result.add(stringValue);
                }else {
                    WormTypeAdapter typeAdapter = Worm.getTypeAdapterRegistry().getTypeAdapter(field.getType());
                    if (typeAdapter != null)
                        stringValue = typeAdapter.toDatabase(value);
                    else
                        stringValue = value.toString();
                }

                builder.append('\'').append(WormUtils.escapeToSql(stringValue)).append('\'');
            } else {
                builder.append("DEFAULT");
            }
            first = false;
        }
        builder.append(")");
        result.set(0,builder.toString());
        return result;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static String uniqueFieldsWhereClause(EntityMeta tableMeta, Entity table, boolean ignoreNull) throws NoSuchFieldException, IllegalAccessException {
        StringBuilder builder = new StringBuilder();

        boolean first = true;
        for (WormField column : tableMeta.getUniqueKeyColumns()) {
            Field field = table.getClass().getDeclaredField(column.getFieldName());
            field.setAccessible(true);
            Object value = field.get(table);

            if (value == null) {
                if (!ignoreNull)
                    throw new NullPointerException("Null value in Primary Key column! Column: " + column.getFieldName() + " | Table: " + tableMeta.getName());
                continue;
            }

            String stringValue;

            WormTypeAdapter typeAdapter = Worm.getTypeAdapterRegistry().getTypeAdapter(field.getType());
            if (typeAdapter != null)
                stringValue = typeAdapter.toDatabase(value);
            else
                stringValue = value.toString();

            builder.append(first ? "" : " AND ").append('`').append(column.getName()).append("`='").append(WormUtils.escapeToSql(stringValue)).append('\'');
            first = false;
        }

        return builder.toString();
    }

    public static String update(EntityMeta meta, HashMap<String, String> values, boolean ignoreNull){
        StringBuilder builder = new StringBuilder("UPDATE ").append(meta.getName()).append(" SET ");
        boolean first = true;
        for(String key : values.keySet()){
            if(Objects.equals(values.get(key), "DEFAULT") && ignoreNull) continue;
            if(!first) builder.append(", ");
            builder.append(key).append("=").append(values.get(key));
            first = false;
        }
        return builder.toString();
    }
}
