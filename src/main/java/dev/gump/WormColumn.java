package dev.gump;

public class WormColumn {
    private final String fieldName;
    private final String sqlName;
    private final String sqlCreation;
    private final boolean idColumn;

    public WormColumn(String columnName, String sqlCreation){
        this.fieldName = columnName;
        this.sqlName = columnName;
        this.sqlCreation = sqlCreation;
        this.idColumn = false;
    }
    public WormColumn(String columnName, String sqlCreation, boolean idColumn){
        this.fieldName = columnName;
        this.sqlName = columnName;
        this.sqlCreation = sqlCreation;
        this.idColumn = idColumn;
    }
    public WormColumn(String columnName, String sqlName, String sqlCreation, boolean idColumn){
        this.fieldName = columnName;
        this.sqlName = sqlName;
        this.sqlCreation = sqlCreation;
        this.idColumn = idColumn;
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

    public boolean isId() {
        return idColumn;
    }
}
