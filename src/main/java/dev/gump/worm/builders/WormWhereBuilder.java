package dev.gump.worm.builders;

import dev.gump.worm.WormException;
import dev.gump.worm.WormUtils;
import dev.gump.worm.entity.EntityMeta;
import dev.gump.worm.entity.WormQueryType;
import dev.gump.worm.entity.WormWhere;
import dev.gump.worm.field.WormField;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class WormWhereBuilder {
    public static String compare(EntityMeta meta, char op, String column, Object value, boolean fields){
        WormField field = meta.getColumn(column);
        if(field == null) throw new WormException("Column " + column + " don't found");
        if(fields) {
            WormField field2 = meta.getColumn(value.toString());
            if (field2 == null) throw new WormException("Column " + column + " don't found");
            return column + op + value;
        }

        if(field.getType().isNeedAsps())
            return column + op + "'" + WormUtils.escapeToSql(value.toString()) + "'";
        else
            return column + op + value.toString();
    }

    public static String like(EntityMeta meta, String column, String like){
        WormField field = meta.getColumn(column);
        if(field == null) throw new WormException("Column " + column + " don't found");
        return column + " LIKE '" + like + "'";
    }

    public static String isNull(EntityMeta meta, String column){
        WormField field = meta.getColumn(column);
        if(field == null) throw new WormException("Column " + column + " don't found");
        return column + " IS NULL";
    }
    public static String isNotNull(EntityMeta meta, String column){
        WormField field = meta.getColumn(column);
        if(field == null) throw new WormException("Column " + column + " don't found");
        return column + " IS NOT NULL";
    }

    public static String order(EntityMeta meta, Order order, String... columns){
        for(String column : columns) {
            WormField field = meta.getColumn(column);
            if (field == null) throw new WormException("Column " + column + " don't found");
        }
        return "ORDER BY " + String.join(",", columns) + " " + order.toString();
    }
    public static String group(EntityMeta meta, String... columns){
        for(String column : columns) {
            WormField field = meta.getColumn(column);
            if (field == null) throw new WormException("Column " + column + " don't found");
        }
        return "GROUP BY " + String.join(",", columns);
    }

    public static String buildWhere(WormOperation... operations){
        return Arrays.stream(operations).map(WormOperation::buildChildrens).collect(Collectors.joining(" AND "));
    }

    public static String queryWithId(EntityMeta meta, WormWhere where, Object... value){
        String columns = "*";
        if(where.getContext() != null)
            columns = meta.getFieldsFromContext(where.getContext());
        else if(where.getFields().size() > 0)
            columns = String.join(",",where.getFields());

        StringBuilder builder = new StringBuilder("SELECT ").append(columns).append(" FROM ").append(meta.getName()).append(" WHERE ");
        if(value.length != meta.getUniqueKeyColumns().size())
            throw new WormException("You need to inform all unique keys on get");

        List<String> wheres = new ArrayList<>();
        int i = 0;
        for (WormField field : meta.getUniqueKeyColumns()) {
            if(field.getType().isNeedAsps())
                wheres.add(field.getName() + " = '" + WormUtils.escapeToSql(value[i].toString()) + "'");
            else
                wheres.add(field.getName() + " = " + value[i]);
            i++;
        }

        builder.append(String.join(" AND ", wheres));

        builder.append(" LIMIT 1 ");

        return builder.toString();
    }

    public static String onlyWhere(WormWhere where){
        StringBuilder builder = new StringBuilder("");

        if(where.getWhere().size() > 0)
            builder.append(" WHERE ").append(buildWhere(where.getWhere().toArray(WormOperation[]::new)));

        if(where.getOrderBy() != null)
            builder.append(" ").append(where.getOrderBy());

        if(where.getGroup() != null)
            builder.append(" ").append(where.getGroup());

        if(where.getType() == WormQueryType.FIND_ONE){
            builder.append(" ").append("LIMIT 1");
        }else {
            if (where.getLimit() != null)
                builder.append(" ").append(where.getLimit());

            if (where.getOffset() != null) {
                if (!(where.getLimit() != null && where.getLimit().contains("OFFSET")))
                    builder.append(" ").append(where.getOffset());
            }
        }

        return builder.toString();
    }

    public static String queryWithWhere(EntityMeta meta, WormWhere where){
        StringBuilder builder = new StringBuilder("SELECT ");
        String columns = "*";
        if(where.getContext() != null)
            columns = meta.getFieldsFromContext(where.getContext());
        else if(where.getFields().size() > 0)
            columns = String.join(",",where.getFields());

        builder.append(where.getType() == WormQueryType.COUNT ? "COUNT("+columns+") as count" : columns).append(" FROM ").append(meta.getName());

        builder.append(onlyWhere(where));

        return builder.toString();
    }
}
