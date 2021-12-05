package dev.gump;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class WormTable implements Cloneable {
    private final List<WormColumn> columns;
    private final String tableName;
    private static List<String> initialized = new ArrayList<>();
    private ResultSet results;

    public WormTable(List<WormColumn> columns){
        this.columns = columns;
        this.tableName = WormUtils.getLastDot(this.getClass().getName());
        Create();
    }

    public Boolean Insert(){
        StringBuilder sql = new StringBuilder("INSERT INTO `" + tableName + "` (");
        boolean first = true;
        for(WormColumn column : columns) {
            sql.append(first ? "" : ", ").append(column.sqlName);
            first = false;
        }
        sql.append(") VALUES (");
        first = true;
        Object obj = this;
        for(WormColumn column : columns) {
            try {
                Field field = obj.getClass().getDeclaredField(column.getName());
                if(field.getGenericType().toString().toUpperCase(Locale.ROOT).contains("STRING"))
                    sql.append(first ? "" : ", ").append("'").append(field.get(obj)).append("'");
                else
                    sql.append(first ? "" : ", ").append(field.get(obj));
            } catch (IllegalAccessException | NoSuchFieldException e) {
                e.printStackTrace();
            }
            first = false;
        }
        sql.append(")");
        try {
            PreparedStatement statement = WormConnector.Query(sql.toString());
            assert statement != null;
            statement.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Boolean Update(){
        WormColumn field = null;
        for(WormColumn column : columns) {
            if(column.getId()){
                field = column;
                break;
            }
        }
        if(field == null)
            throw new IllegalArgumentException("No id.");
        Object obj = this;
        try {
            Field f = obj.getClass().getField(field.getName());
            if(f.getGenericType().toString().toUpperCase(Locale.ROOT).contains("STRING"))
                return Update(field.getSqlName() + " = '" + f.get(obj).toString() + "'");
            else
                return Update(field.getSqlName() + " = " + f.get(obj).toString());
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Boolean Update(String where){
        StringBuilder sql = new StringBuilder("UPDATE `" + tableName + "` SET ");
        boolean first = true;
        Object obj = this;
        for(WormColumn column : columns) {
            sql.append(first ? "" : ", ").append(column.sqlName).append(" = ");
            try {
                Field field = obj.getClass().getDeclaredField(column.getName());
                if(field.getGenericType().toString().toUpperCase(Locale.ROOT).contains("STRING"))
                    sql.append("'").append(field.get(obj)).append("'");
                else
                    sql.append(field.get(obj));
            } catch (IllegalAccessException | NoSuchFieldException e) {
                e.printStackTrace();
            }
            first = false;
        }
        sql.append(" WHERE ").append(where);
        try {
            PreparedStatement statement = WormConnector.Query(sql.toString());
            assert statement != null;
            statement.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Boolean Delete(){
        WormColumn field = null;
        for(WormColumn column : columns) {
            if(column.getId()){
                field = column;
                break;
            }
        }
        if(field == null)
            throw new IllegalArgumentException("No id.");
        Object obj = this;
        try {
            Field f = obj.getClass().getField(field.getName());
            if(f.getGenericType().toString().toUpperCase(Locale.ROOT).contains("STRING"))
                return Delete("where " +field.getSqlName() + " = '" + f.get(obj).toString() + "'");
            else
                return Delete("where " +field.getSqlName() + " = " + f.get(obj).toString());
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Boolean Delete(String where){
        String sql = "DELETE FROM `" + tableName + "` " + where;
        try {
            PreparedStatement statement = WormConnector.Query(sql);
            assert statement != null;
            statement.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Boolean Get(Integer id){
        String field = "";
        for(WormColumn column : columns) {
            if(column.getId()){
                field = column.sqlName;
                break;
            }
        }
        if(field.equals(""))
            throw new IllegalArgumentException("No id.");

        return Find(field + " = " + id);
    }

    public Boolean Get(String id){
        String field = "";
        for(WormColumn column : columns) {
            if(column.getId()){
                field = column.sqlName;
                break;
            }
        }
        if(field.equals(""))
            throw new IllegalArgumentException("No id.");

        return Find(field + " = '" + id + "'");
    }

    public Boolean Next(){
        try {
            if(results.next()){
                Object object = this;
                for (WormColumn column : columns) {
                    Field field = object.getClass().getDeclaredField(column.getName());
                    if (field.getGenericType().toString().toUpperCase(Locale.ROOT).contains("STRING"))
                        field.set(object, results.getString(column.sqlName));
                    else if (field.getGenericType().toString().toUpperCase(Locale.ROOT).contains("INT"))
                        field.set(object, results.getInt(column.sqlName));
                    else if (field.getGenericType().toString().toUpperCase(Locale.ROOT).contains("BOOL"))
                        field.set(object, results.getBoolean(column.sqlName));
                    else if (field.getGenericType().toString().toUpperCase(Locale.ROOT).contains("DOUBLE"))
                        field.set(object, results.getDouble(column.sqlName));
                    else if (field.getGenericType().toString().toUpperCase(Locale.ROOT).contains("FLOAT"))
                        field.set(object, results.getFloat(column.sqlName));
                    else
                        throw new IllegalArgumentException("Bad type.");
                }
                return true;
            }else return false;
        } catch (SQLException | NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Boolean Find(){
        return Find("");
    }

    public Boolean Find(String where){
        String sql = "SELECT * FROM `" + tableName + "`" + (!where.equals("") ? " WHERE " + where : "");
        try {
            PreparedStatement statement = WormConnector.Query(sql);
            assert statement != null;
            ResultSet resultSet = statement.executeQuery();
            results = resultSet;
            if(resultSet != null)
                return Next();
            else
                return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    void Create(){
        if(initialized.contains(tableName)) return;
        StringBuilder sql = new StringBuilder("CREATE TABLE IF NOT EXISTS `" + tableName + "` (");
        String primaryKey = "";
        boolean first = true;
        for(WormColumn column : columns) {
            sql.append(first ? "" : ", ").append(column.sqlName).append(" ").append(column.sqlCreation);
            if(column.getId())
                primaryKey = ", PRIMARY KEY (`"+column.sqlName+"`)";
            first = false;
        }
        sql.append(!primaryKey.equals("") ? primaryKey : "").append(")");
        try {
            PreparedStatement statement = WormConnector.Query(sql.toString());
            assert statement != null;
            statement.executeUpdate();
            initialized.add(tableName);
        } catch (SQLException e) {
            e.printStackTrace();
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
}
