package dev.gump;

public class WormColumn {
    String name = "",
            sqlName = "",
            sqlCreation = "";
    Boolean id = false;

    public WormColumn(String columnName, String sqlCreation){
        this.name = columnName;
        this.sqlName = columnName;
        this.sqlCreation = sqlCreation;
    }
    public WormColumn(String columnName, String sqlCreation, Boolean isId){
        this.name = columnName;
        this.sqlName = columnName;
        this.id = isId;
        this.sqlCreation = sqlCreation;
    }
    public WormColumn(String columnName, String sqlName, String sqlCreation, Boolean isId){
        this.name = columnName;
        this.sqlName = sqlName;
        this.sqlCreation = sqlCreation;
        this.id = isId;
    }

    public String getName() {
        return name;
    }

    public String getSqlName() {
        return sqlName;
    }

    public String getSqlCreation() {
        return sqlCreation;
    }

    public Boolean getId() {
        return id;
    }
}
