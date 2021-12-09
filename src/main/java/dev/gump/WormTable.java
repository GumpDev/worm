package dev.gump;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WormTable implements Cloneable, AutoCloseable {
    private static final List<String> initializedTables = new ArrayList<>();

    private final List<WormColumn> columns;
    private final String tableName;
    private WormQuery query;
    private final Logger logger;

    public WormTable(List<WormColumn> columns) {
        this.columns = columns;
        this.tableName = WormUtils.getLastDot(this.getClass().getName());
        this.logger = Logger.getLogger(this.tableName + " Logger");

        Create();
    }

    public WormTable(String tableName, List<WormColumn> columns) {
        this.columns = columns;
        this.tableName = tableName;
        this.logger = Logger.getLogger(this.tableName + " Logger");

        Create();
    }

    public Boolean Insert() {
        StringBuilder sql = new StringBuilder("INSERT INTO `" + tableName + "` (");
        boolean first = true;
        for(WormColumn column : columns) {
            sql.append(first ? "" : ", ").append(column.getSqlName());
            first = false;
        }
        sql.append(") VALUES (");
        first = true;
        for(WormColumn column : columns) {
            try {
                Field field = this.getClass().getDeclaredField(column.getFieldName());
                field.setAccessible(true);
                String value = WormUtils.escapeToSql(field.get(this).toString());

                sql.append(first ? "" : ", ").append("'").append(value).append("'");
            } catch (Exception exception) {
                logger.log(Level.SEVERE, "Something went wrong while collecting " + this.tableName + "'s " + column.getFieldName() + " value", exception);
                return false;
            }
            first = false;
        }
        sql.append(")");

        try (WormQuery query = WormConnector.Query(sql.toString())) {
            query.executeUpdate();
            return true;
        } catch (SQLException exception) {
            logger.log(Level.SEVERE, "Something went wrong while inserting into table " + this.tableName, exception);
            return false;
        }
    }

    public Boolean Update() {
        WormColumn idColumn = getIdColumn();
        if (idColumn == null) return false;

        try {
            Field field = this.getClass().getField(idColumn.getFieldName());
            field.setAccessible(true);
            String value = WormUtils.escapeToSql(field.get(this).toString());

            return Update(idColumn.getSqlName() + " = '" + value + "'");
        } catch (Exception exception) {
            logger.log(Level.SEVERE, "Something went wrong while updating into table " + this.tableName + " with id column", exception);
            return false;
        }
    }

    public Boolean Update(String where) {
        StringBuilder sql = new StringBuilder("UPDATE `" + tableName + "` SET ");
        boolean first = true;
        Object obj = this;
        for(WormColumn column : columns) {
            sql.append(first ? "" : ", ").append(column.getSqlName()).append(" = ");
            try {
                Field field = obj.getClass().getDeclaredField(column.getFieldName());
                field.setAccessible(true);
                sql.append("'").append(field.get(obj)).append("'");
            } catch (Exception exception) {
                logger.log(Level.SEVERE, "Something went wrong while collecting " + this.tableName + "'s " + column.getFieldName() + " value", exception);
                return false;
            }
            first = false;
        }
        sql.append(" WHERE ").append(where);

        try (WormQuery query = WormConnector.Query(sql.toString())) {
            query.executeUpdate();
            return true;
        } catch (SQLException exception) {
            logger.log(Level.SEVERE, "Something went wrong while inserting into table " + this.tableName, exception);
            return false;
        }
    }

    public Boolean Delete() {
        WormColumn idColumn = getIdColumn();
        if (idColumn == null) return false;

        try {
            Field field = this.getClass().getField(idColumn.getFieldName());
            field.setAccessible(true);
            String value = WormUtils.escapeToSql(field.get(this).toString());

            return Delete("WHERE " + idColumn.getSqlName() + " = '" + value + "'");
        } catch (Exception exception) {
            logger.log(Level.SEVERE, "Something went wrong while deleting from table " + this.tableName + " with id column", exception);
            return false;
        }
    }

    public Boolean Delete(String where) {
        String sql = "DELETE FROM `" + tableName + "` " + where;

        try (WormQuery query = WormConnector.Query(sql)) {
            query.executeUpdate();
            return true;
        } catch (SQLException exception) {
            logger.log(Level.SEVERE, "Something went wrong while deleting from table " + this.tableName, exception);
            return false;
        }
    }

    public Boolean Get(Integer id) {
        WormColumn idColumn = getIdColumn();
        if (idColumn == null) return false;

        return Find(idColumn.getSqlName() + " = " + id);
    }

    public Boolean Get(String id) {
        WormColumn idColumn = getIdColumn();
        if (idColumn == null) return false;

        return Find(idColumn.getSqlName() + " = '" + id + "'");
    }

    public Boolean Next(){
        if (query == null) {
            logger.severe("Cannot advance with ResultSet without a query");
            return false;
        }

        try {
            ResultSet set = query.getResultSet();

            if (set == null) {
                logger.severe("Cannot advance with ResultSet without a query");
                query.close();
                return false;
            }

            if (!set.next()) {
                query.close();
                return false;
            }

            for (WormColumn column : columns) {
                Field field = this.getClass().getDeclaredField(column.getFieldName());
                String type = field.getGenericType().toString().toUpperCase(Locale.ROOT);
                field.setAccessible(true);

                if (type.contains("STRING"))
                    field.set(this, set.getString(column.getSqlName()));
                else if (type.contains("INT"))
                    field.set(this, set.getInt(column.getSqlName()));
                else if (type.contains("BOOL"))
                    field.set(this, set.getBoolean(column.getSqlName()));
                else if (type.contains("DOUBLE"))
                    field.set(this, set.getDouble(column.getSqlName()));
                else if (type.contains("FLOAT"))
                    field.set(this, set.getFloat(column.getSqlName()));
                else
                    throw new IllegalArgumentException(type + " type is not supported by Worm");
            }

            return true;
        } catch (Exception exception) {
            logger.log(Level.SEVERE, "Something went wrong while advancing to next row in table " + this.tableName, exception);
            return false;
        }
    }

    public Boolean Find() {
        return Find("");
    }

    public Boolean Find(String where) {
        String sql = "SELECT * FROM `" + tableName + "`" + (!where.equals("") ? " WHERE " + where : "");

        try {
            this.query = WormConnector.Query(sql);
            this.query.executeQuery();

            return Next();
        } catch (SQLException exception) {
            logger.log(Level.SEVERE, "Something went wrong while finding in table " + this.tableName, exception);
            return false;
        }
    }

    void Create(){
        if(initializedTables.contains(tableName)) return;

        StringBuilder sql = new StringBuilder("CREATE TABLE IF NOT EXISTS `" + tableName + "` (");
        String primaryKey = "";
        boolean first = true;
        for(WormColumn column : columns) {
            sql.append(first ? "" : ", ").append(column.getSqlName()).append(" ").append(column.getSqlCreation());
            if(column.isId())
                primaryKey = ", PRIMARY KEY (`"+column.getSqlName()+"`)";
            first = false;
        }
        sql.append(!primaryKey.equals("") ? primaryKey : "").append(")");

        try (WormQuery query = WormConnector.Query(sql.toString())) {
            query.executeUpdate();
            initializedTables.add(tableName);
        } catch (SQLException exception) {
            logger.log(Level.SEVERE, "Something went wrong while deleting from table " + this.tableName, exception);
        }
    }

    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    @Override
    public void close() throws SQLException {
        if (query != null) query.close();
    }

    private WormColumn getIdColumn() {
        WormColumn field = null;
        for(WormColumn column : columns) {
            if(column.isId()){
                field = column;
                break;
            }
        }

        if(field == null) {
            logger.severe("Could not find a id column in table " + this.tableName);
            return null;
        }

        return field;
    }
}
