package dev.gump.worm;

public class WormColumn {
    private final String fieldName;
    private final String sqlName;
    private final String  sqlCreation;
    private final boolean primaryKey;
    private final boolean autoIncrement;

    @Deprecated
    public WormColumn(String columnName, String sqlCreation) {
        this.fieldName = columnName;
        this.sqlName = columnName;
        this.sqlCreation   = sqlCreation;
        this.primaryKey    = false;
        this.autoIncrement = false;
    }

    @Deprecated
    public WormColumn(String columnName, String sqlCreation, boolean primaryKey) {
        this.fieldName     = columnName;
        this.sqlName       = columnName;
        this.sqlCreation   = sqlCreation;
        this.primaryKey    = primaryKey;
        this.autoIncrement = false;
    }

    @Deprecated
    public WormColumn(String columnName, String sqlName, String sqlCreation, boolean primaryKey) {
        this.fieldName     = columnName;
        this.sqlName       = sqlName;
        this.sqlCreation   = sqlCreation;
        this.primaryKey    = primaryKey;
        this.autoIncrement = false;
    }

    @Deprecated
    public WormColumn(String columnName, String sqlCreation, boolean primaryKey, boolean autoIncrement) {
        this.fieldName     = columnName;
        this.sqlName       = columnName;
        this.sqlCreation   = sqlCreation;
        this.primaryKey    = primaryKey;
        this.autoIncrement = autoIncrement;
    }

    public WormColumn(String columnName, String sqlName, String sqlCreation, boolean primaryKey, boolean autoIncrement) {
        this.fieldName     = columnName;
        this.sqlName       = sqlName;
        this.sqlCreation   = sqlCreation;
        this.primaryKey    = primaryKey;
        this.autoIncrement = autoIncrement;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getSqlName() {
        return sqlName;
    }

    public String getSqlCreation() {
        return sqlCreation;
    }

    public boolean isPrimaryKey() {
        return primaryKey;
    }

    public boolean isAutoIncrement() {
        return autoIncrement;
    }
}
