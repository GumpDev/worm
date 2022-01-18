package dev.gump.worm;

import java.lang.reflect.Field;
import java.util.Iterator;

public class WormQueryBuilders {

    static String buildCreateTableQuery(WormTableMeta tableMeta) {
        StringBuilder builder = new StringBuilder("CREATE TABLE IF NOT EXISTS `").append(tableMeta.getName()).append("`(");

        boolean first = true;
        for (WormColumn column : tableMeta.getAllColumns()) {
            builder.append(first ? "" : ", ").append('`').append(column.getSqlName()).append("` ").append(column.getSqlCreation());

            first = false;
        }

        builder.append(", PRIMARY KEY(");
        first = true;
        for (WormColumn column : tableMeta.getPrimaryKeyColumns()) {
            builder.append(first ? "" : ", ").append('`').append(column.getSqlName()).append('`');

            first = false;
        }

        builder.append("))");

        return builder.toString();
    }

    static String buildInsertQuery(WormTableMeta tableMeta, WormTable table) throws NoSuchFieldException, IllegalAccessException {
        StringBuilder builder = new StringBuilder("INSERT INTO `").append(tableMeta.getName()).append("`(");
        boolean first = true;
        for (WormColumn column : tableMeta.getAllColumns()) {
            builder.append(first ? "" : ", ").append('`').append(column.getSqlName()).append("`");

            first = false;
        }
        builder.append(") VALUES (");

        first = true;
        for (WormColumn column : tableMeta.getAllColumns()) {
            Field field = table.getClass().getDeclaredField(column.getFieldName());
            field.setAccessible(true);
            Object value = field.get(table);

            builder.append(first ? "" : ", ");

            if (value != null) {
                builder.append('\'').append(WormUtils.escapeToSql(value.toString())).append('\'');
            } else {
                builder.append("DEFAULT");
            }
            first = false;
        }
        builder.append(")");

        return builder.toString();
    }

    static String buildUpdateQuery(WormTableMeta tableMeta, WormTable table, String whereClause, int limit) throws NoSuchFieldException, IllegalAccessException {
        StringBuilder builder = new StringBuilder("UPDATE `").append(tableMeta.getName()).append("` SET ");

        boolean first = true;
        for (WormColumn column : tableMeta.getAllColumns()) {
            Field field = table.getClass().getDeclaredField(column.getFieldName());
            field.setAccessible(true);
            Object value = field.get(table);

            builder.append(first ? "" : ", ").append('`').append(column.getSqlName()).append("`=");

            if (value != null)
                builder.append('\'').append(WormUtils.escapeToSql(value.toString())).append('\'');
            else
                builder.append("DEFAULT");

            first = false;
        }

        builder.append(" WHERE ").append(whereClause);

        if (limit > 0)
            builder.append(" LIMIT ").append(limit);

        return builder.toString();
    }

    static String buildPrimaryKeyWhereClause(WormTableMeta tableMeta, WormTable table, boolean ignoreNull) throws NoSuchFieldException, IllegalAccessException {
        StringBuilder builder = new StringBuilder();

        boolean first = true;
        for (WormColumn column : tableMeta.getPrimaryKeyColumns()) {
            Field field = table.getClass().getField(column.getFieldName());
            field.setAccessible(true);
            Object value = field.get(table);

            if (value == null) {
                if (!ignoreNull)
                    throw new NullPointerException("Null value in Primary Key column! Column: " + column.getFieldName() + " | Table: " + tableMeta.getName());
                continue;
            }

            builder.append(first ? "" : " AND ").append('`').append(column.getSqlName()).append("`='").append(WormUtils.escapeToSql(value.toString())).append('\'');
            first = false;
        }

        return builder.toString();
    }

    static String buildCustomPrimaryKeyWhereClause(WormTableMeta tableMeta, Object[] values) {
        StringBuilder builder = new StringBuilder();
        Iterator<WormColumn> iterator = tableMeta.getPrimaryKeyColumns().iterator();

        boolean first = true;
        for (Object value : values) {
            WormColumn column = iterator.next();

            if (value == null) continue;

            builder.append(first ? "" : " AND ").append('`').append(column.getSqlName()).append("`='").append(WormUtils.escapeToSql(value.toString())).append('\'');
            first = false;
        }

        return builder.toString();
    }

}
