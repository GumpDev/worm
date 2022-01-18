package dev.gump.worm;

import java.lang.reflect.Field;
import java.util.*;

public final class WormRegistry {

    private final Map<Class<? extends WormTable>, WormTableMeta> tableMap = new HashMap<>();

    WormRegistry() {}

    public void registerTable(String tableName, Class<? extends WormTable> tableClass) throws WormException {
        List<WormColumn> commonColumns = new ArrayList<>();
        List<WormColumn> primaryKeyColumns = new ArrayList<>();
        WormColumn autoIncrementColumn = null;

        for (Field field : tableClass.getDeclaredFields()) {
            field.setAccessible(true);
            WormField wormField = field.getAnnotation(WormField.class);
            if (wormField == null) continue;

            String fieldName = field.getName();
            String sqlName = wormField.sqlName();
            String sqlType = wormField.sqlType();
            String defaultValue = wormField.defaultValue();
            int length = wormField.length();
            boolean autoIncrement = wormField.autoIncrement();
            boolean primaryKey = wormField.primaryKey();
            boolean nullable = wormField.nullable();

            if (primaryKey && nullable)
                throw new WormException("Primary Key columns cannot be nullable! Column: " + fieldName + " | Table " + tableName);

            if (autoIncrement && !primaryKey)
                throw new WormException("Auto Increment is only allowed in a Primary Key column! Column: " + fieldName + " | Table " + tableName);

            if (autoIncrement && autoIncrementColumn != null)
                throw new WormException("Only one Auto Increment column is allowed! Column: " + fieldName + " | Table " + tableName);

            if (Objects.equals(sqlName, ""))
                sqlName = fieldName;

            StringBuilder sqlSpec = new StringBuilder(sqlType);
            if (length > 0)
                sqlSpec.append('(').append(length).append(')');
            if (!nullable)
                sqlSpec.append(" NOT NULL");
            if (autoIncrement)
                sqlSpec.append(" AUTO_INCREMENT");
            if (!Objects.equals(defaultValue, ""))
                sqlSpec.append(" DEFAULT '").append(defaultValue).append('\'');

            WormColumn column = new WormColumn(fieldName, sqlName, sqlSpec.toString(), primaryKey, autoIncrement);

            if (autoIncrement) {
                autoIncrementColumn = column;
                continue;
            }

            if (primaryKey) {
                primaryKeyColumns.add(column);
                continue;
            }

            commonColumns.add(column);
        }

        if (primaryKeyColumns.size() == 0)
            throw new WormException("At least one Primary Key column should exists! Table " + tableName);

        WormTableMeta tableMeta = new WormTableMeta(tableName, commonColumns, primaryKeyColumns, autoIncrementColumn);
        this.tableMap.put(tableClass, tableMeta);
    }

    public WormTableMeta getTableMeta(Class<? extends WormTable> tableClass) {
        return Objects.requireNonNull(this.tableMap.get(tableClass), "Table Class " + tableClass.getName() + " was not registered!");
    }

    Collection<WormTableMeta> getTables() {
        return this.tableMap.values();
    }

}
