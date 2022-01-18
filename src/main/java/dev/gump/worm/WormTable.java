package dev.gump.worm;

import dev.gump.worm.typeadapter.WormTypeAdapter;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class WormTable implements AutoCloseable, Cloneable {
    private final WormTableMeta tableMeta;
    private WormQuery currentQuery;

    public WormTable() {
        this.tableMeta = Worm.getRegistry().getTableMeta(getClass());
    }

    //region Insert
    /**
     * Inserts a new row with the instance data.
     *
     * @return If the operation was successful.
     */
    public boolean insert() {
        try {
            String strQuery = WormQueryBuilders.buildInsertQuery(tableMeta, this);
            WormColumn autoIncrementColumn = tableMeta.getAutoIncrementColumn();

            WormQuery query = autoIncrementColumn == null ? Worm.query(strQuery) : Worm.queryWithGeneratedKeys(strQuery);
            try (query) {
                query.executeUpdate();

                if (autoIncrementColumn != null) {
                    ResultSet generatedKeys = query.getStatement().getGeneratedKeys();

                    if (generatedKeys.next()) {
                        int generatedId = generatedKeys.getInt(1);
                        Field field = this.getClass().getDeclaredField(autoIncrementColumn.getFieldName());
                        field.setAccessible(true);
                        field.set(this, generatedId);
                    }
                }
            }

            return true;
        } catch (Exception exception) {
            Worm.getLogger().error("Something went wrong while inserting table " + tableMeta.getName(), exception);
            return false;
        }
    }
    //endregion

    //region Next
    /**
     * Parses the next row of the current query on this instance.
     *
     * @return If the operation was successful.
     */
    public boolean next() { return this.next(this); }

    /**
     * Parses the next row of the current query on {@code instance} parameter.
     *
     * @param instance The instance that will receive the values.
     * @return If the operation was successful.
     */
    public boolean next(WormTable instance) {
        try {
            Objects.requireNonNull(currentQuery, "Cannot advance without a query!");

            if (currentQuery.getStatement().isClosed())
                throw new WormException("Statement was closed");

            ResultSet set = currentQuery.getResultSet();

            if (set == null) {
                currentQuery.close();
                throw new WormException("Result Set is null");
            }

            // If no more rows close query
            if (!set.next()) {
                currentQuery.close();
                return false;
            }

            // Parse all known types
            for (WormColumn column : tableMeta.getAllColumns()) {
                Field field = instance.getClass().getDeclaredField(column.getFieldName());
                field.setAccessible(true);

                if (set.getObject(column.getSqlName()) == null) {
                    field.set(instance, null);
                    continue;
                }

                Class<?> type = field.getType();

                // Java Primitives
                if (type == boolean.class || type == Boolean.class)
                    field.set(instance, set.getBoolean(column.getSqlName()));
                else if (type == char.class || type == Character.class)
                    field.set(instance, set.getString(column.getSqlName()).charAt(0));
                else if (type == byte.class || type == Byte.class)
                    field.set(instance, set.getByte(column.getSqlName()));
                else if (type == short.class || type == Short.class)
                    field.set(instance, set.getShort(column.getSqlName()));
                else if (type == int.class || type == Integer.class)
                    field.set(instance, set.getInt(column.getSqlName()));
                else if (type == long.class || type == Long.class)
                    field.set(instance, set.getLong(column.getSqlName()));
                else if (type == float.class || type == Float.class)
                    field.set(instance, set.getFloat(column.getSqlName()));
                else if (type == double.class || type == Double.class)
                    field.set(instance, set.getDouble(column.getSqlName()));
                else if (type == String.class)
                    field.set(instance, set.getString(column.getSqlName()));
                else {
                    @SuppressWarnings("rawtypes")
                    WormTypeAdapter typeAdapter = Worm.getTypeAdapterRegistry().getTypeAdapter(type);
                    if (typeAdapter == null)
                        throw new WormException(type.getName() + " type is not supported!");

                    Object result = typeAdapter.fromDatabase(set, column.getSqlName());
                    field.set(instance, result);
                }
            }

            return true;
        } catch (Exception exception) {
            Worm.getLogger().error("Something went wrong while advancing to next row in table " + tableMeta.getName(), exception);
            return false;
        }
    }
    //endregion

    //region Update
    /**
     * Updates one row with the current data of this instance using this instance's primary key values as the query's primary key values in the where clause.
     *
     * @return {@code true} if the operation was successful, {@code false} if one or more primary key values are null or something else went wrong.
     */
    public boolean update() {
        return update(0);
    }

    /**
     * Updates one row with the current data of this instance using this instance's primary key values as the query's primary key values in the where clause.
     *
     * @param limit The limit of rows that will be updated.
     * @return {@code true} if the operation was successful, {@code false} if one or more primary key values are null or something else went wrong.
     */
    public boolean update(int limit) {
        try {
            String whereClause = WormQueryBuilders.buildPrimaryKeyWhereClause(tableMeta, this, false);
            return updateWhere(whereClause, limit);
        } catch (Exception exception) {
            Worm.getLogger().error("Something went wrong while updating table " + tableMeta.getName(), exception);
            return false;
        }
    }

    /**
     * Update row(s) with the current data of this instance using {@code keys} as the query's primary key values in the where clause.
     *
     * @param keys The primary key values. It should follow primary key field definition order, and it can be partial.
     * @return {@code true} if the operation was successful, {@code false} if {@code keys} is invalid or something else went wrong.
     */
    public boolean updateWithCustomKeys(Object... keys) {
        return updateWithCustomKeys(0, keys);
    }

    /**
     * Update row(s) with the current data of this instance using {@code keys} as the query's primary key values in the where clause.
     *
     * @param limit The limit of rows that will be updated.
     * @param keys The primary key values. It should follow primary key field definition order, and it can be partial.
     * @return {@code true} if the operation was successful, {@code false} if {@code keys} is invalid or something else went wrong.
     */
    public boolean updateWithCustomKeys(int limit, Object... keys) {
        try {
            if (keys == null || keys.length == 0) return false;
            if (tableMeta.getPrimaryKeyColumns().size() != keys.length)
                throw new WormException("Wrong number of keys! Table primary key count: " + tableMeta.getPrimaryKeyColumns().size() + " | Argument count: " + keys.length + " | Table: " + tableMeta.getName());

            String whereClause = WormQueryBuilders.buildCustomPrimaryKeyWhereClause(tableMeta, keys);
            return updateWhere(whereClause, limit);
        } catch (Exception exception) {
            Worm.getLogger().error("Something went wrong while updating in table " + tableMeta.getName(), exception);
            return false;
        }
    }

    /**
     * Update row(s) with the current data of this instance using {@code whereClause} as the query's where clause.
     *
     * @param whereClause The where clause.
     * @return If the operation was successful.
     */
    public boolean updateWhere(String whereClause) { return this.updateWhere(whereClause, 0); }

    /**
     * Update row(s) with the current data of this instance using {@code whereClause} as the query's where clause.
     *
     * @param whereClause The where clause.
     * @param limit The limit of rows that will be updated.
     * @return If the operation was successful.
     */
    public boolean updateWhere(String whereClause, int limit) {
        try {
            String strQuery = WormQueryBuilders.buildUpdateQuery(tableMeta, this, whereClause, limit);
            try (WormQuery query = Worm.query(strQuery)) {
                query.executeUpdate();
            }
            return true;
        } catch (Exception exception) {
            Worm.getLogger().error("Something went wrong while updating table " + tableMeta.getName(), exception);
            return false;
        }
    }
    //endregion

    //region Delete
    /**
     * Delete one row using this instance's primary key values as the query's primary key values in the where clause.
     *
     * @return {@code true} if the operation was successful, {@code false} if one or more primary key values are null or something else went wrong.
     */
    public boolean delete() {
        return delete(0);
    }

    /**
     * Delete one row using this instance's primary key values as the query's primary key values in the where clause.
     *
     * @param limit The limit of rows that will be deleted.
     * @return {@code true} if the operation was successful, {@code false} if one or more primary key values are null or something else went wrong.
     */
    public boolean delete(int limit) {
        try {
            String whereClause = WormQueryBuilders.buildPrimaryKeyWhereClause(tableMeta, this, false);
            return deleteWhere(whereClause, limit);
        } catch (Exception exception) {
            Worm.getLogger().error("Something went wrong while deleting table " + tableMeta.getName(), exception);
            return false;
        }
    }

    /**
     * Delete row(s) using {@code keys} as the query's primary key values in the where clause.
     *
     * @param keys The primary key values. It should follow primary key field definition order, and it can be partial.
     * @return {@code true} if the operation was successful, {@code false} if {@code keys} is invalid or something else went wrong.
     */
    public boolean deleteWithCustomKeys(Object... keys) {
        return deleteWithCustomKeys(0, keys);
    }

    /**
     * Delete row(s) using {@code keys} as the query's primary key values in the where clause.
     *
     * @param limit The limit of rows that will be deleted.
     * @param keys The primary key values. It should follow primary key field definition order, and it can be partial.
     * @return {@code true} if the operation was successful, {@code false} if {@code keys} is invalid or something else went wrong.
     */
    public boolean deleteWithCustomKeys(int limit, Object... keys) {
        try {
            if (keys == null || keys.length == 0) return false;
            if (tableMeta.getPrimaryKeyColumns().size() != keys.length)
                throw new WormException("Wrong number of keys! Table primary key count: " + tableMeta.getPrimaryKeyColumns().size() + " | Argument count: " + keys.length + " | Table: " + tableMeta.getName());

            String whereClause = WormQueryBuilders.buildCustomPrimaryKeyWhereClause(tableMeta, keys);
            return deleteWhere(whereClause, limit);
        } catch (Exception exception) {
            Worm.getLogger().error("Something went wrong while deleting in table " + tableMeta.getName(), exception);
            return false;
        }
    }

    /**
     * Delete row(s) using {@code whereClause} as the query's where clause.
     *
     * @param whereClause The where clause.
     * @return If the operation was successful.
     */
    public boolean deleteWhere(String whereClause) { return this.deleteWhere(whereClause, 0); }

    /**
     * Delete row(s) using {@code whereClause} as the query's where clause.
     *
     * @param whereClause The where clause.
     * @param limit The limit of rows that will be deleted.
     * @return If the operation was successful.
     */
    public boolean deleteWhere(String whereClause, int limit) {
        try {
            StringBuilder sqlBuilder = new StringBuilder("DELETE FROM `").append(tableMeta.getName()).append("`");

            if (whereClause != null)
                sqlBuilder.append(" WHERE ").append(whereClause);

            if (limit > 0)
                sqlBuilder.append(" LIMIT ").append(limit);

            try (WormQuery query = Worm.query(sqlBuilder.toString())) {
                query.executeUpdate();
            }

            return true;
        } catch (Exception exception) {
            Worm.getLogger().error("Something went wrong while deleting table " + tableMeta.getName(), exception);
            return false;
        }
    }
    //endregion

    //region Find
    /**
     * Finds and parses the row that match this instance's primary key values in the where clause.
     *
     * @return {@code true} if the operation was successful, {@code false} if one or more primary key values are null or something else went wrong.
     */
    public boolean find() {
        try {
            String whereClause = WormQueryBuilders.buildPrimaryKeyWhereClause(tableMeta, this, false);
            if (!findWhere(whereClause, 0, 1)) return false;

            return next();
        } catch (Exception exception) {
            Worm.getLogger().error("Something went wrong while finding in table " + tableMeta.getName(), exception);
            return false;
        }
    }

    /**
     * Finds and parses the row that match {@code keys} values in the where clause.
     *
     * @param keys The primary key values. It should follow primary key field definition order, and it can be partial.
     * @return {@code true} if the operation was successful, {@code false} if {@code keys} is invalid or something else went wrong.
     */
    public boolean findWithCustomKeys(Object... keys) {
        try {
            if (keys == null || keys.length == 0) return false;
            if(tableMeta.getPrimaryKeyColumns().size() != keys.length)
                throw new WormException("Wrong number of keys! Table primary key count: " + tableMeta.getPrimaryKeyColumns().size() + " | Argument count: " + keys.length + " | Table: " + tableMeta.getName());

            String whereClause = WormQueryBuilders.buildCustomPrimaryKeyWhereClause(tableMeta, keys);
            if (!findWhere(whereClause, 0, 1)) return false;

            return next();
        } catch (Exception exception) {
            Worm.getLogger().error("Something went wrong while finding in table " + tableMeta.getName(), exception);
            return false;
        }
    }

    /**
     * Finds and parses the row using {@code whereClause} as the query's where clause.
     *
     * @param whereClause The where clause.
     * @return If the operation was successful
     */
    public boolean findWhere(String whereClause) {
        return findWhere(whereClause, 0, 0);
    }

    /**
     * Finds and parses the row using {@code whereClause} as the query's where clause.
     *
     * @param whereClause The where clause.
     * @param limit The limit of rows that will be found.
     * @return If the operation was successful.
     */
    public boolean findWhere(String whereClause, int limit) {
        return findWhere(whereClause, 0, limit);
    }

    /**
     * Finds and parses the row using {@code whereClause} as the query's where clause.
     *
     * @param whereClause The where clause.
     * @param limit The limit of rows that will be found.
     * @return If the operation was successful.
     */
    public boolean findWhere(String whereClause, int offset, int limit) {
        try {
            StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM `").append(tableMeta.getName()).append('`');

            if (whereClause != null)
                sqlBuilder.append(" WHERE ").append(whereClause);

            if(limit > 0) {
                if (offset > 0)
                    sqlBuilder.append(" LIMIT ").append(offset).append(',').append(limit);
                else
                    sqlBuilder.append(" LIMIT ").append(limit);
            }

            this.currentQuery = Worm.query(sqlBuilder.toString());
            this.currentQuery.executeQuery();

            return true;
        } catch (Exception exception) {
            Worm.getLogger().error("Something went wrong while finding in table " + tableMeta.getName(), exception);
            return false;
        }
    }
    //endregion

    //region FindAll
    /**
     * Finds all rows matching {@code keys} values in where clause and returns it.
     *
     * @param keys The primary key values. It should follow primary key field definition order, and it can be partial.
     * @param <T> The type of the results.
     * @return If the operation was successful.
     */
    @Nullable
    public <T extends WormTable> List<T> findAllWithKeys(Object... keys) {
        return findAllWithKeys(0, 0, keys);
    }

    /**
     * Finds all rows matching {@code keys} values in where clause and returns it.
     *
     * @param limit The limit of rows the result will contain.
     * @param keys The primary key values. It should follow primary key field definition order, and it can be partial.
     * @param <T> The type of the results.
     * @return If the operation was successful.
     */
    @Nullable
    public <T extends WormTable> List<T> findAllWithKeys(int limit, Object... keys) {
        return findAllWithKeys(0, limit, keys);
    }

    /**
     * Finds all rows matching {@code keys} values in where clause and returns it.
     *
     * @param offset The offset the results will begin at.
     * @param limit The limit of rows the result will contain.
     * @param keys The primary key values. It should follow primary key field definition order, and it can be partial.
     * @param <T> The type of the results.
     * @return Whether the operation was successful.
     */
    @Nullable
    public <T extends WormTable> List<T> findAllWithKeys(int offset, int limit, Object... keys) {
        try {
            if (keys == null || keys.length == 0) return null;
            if (tableMeta.getPrimaryKeyColumns().size() != keys.length)
                throw new WormException("Wrong number of keys! Table primary key count: " + tableMeta.getPrimaryKeyColumns().size() + " | Argument count: " + keys.length + " | Table: " + tableMeta.getName());

            String whereClause = WormQueryBuilders.buildCustomPrimaryKeyWhereClause(tableMeta, keys);

            return findAllWhere(whereClause, offset, limit);
        } catch (Exception exception) {
            Worm.getLogger().error("Something went wrong while finding in table " + tableMeta.getName(), exception);
            return null;
        }
    }

    /**
     * Finds all rows and returns it.
     *
     * @param <T> The type of the results
     * @return {@code List} if the operation was successful, {@code null} if something went wrong.
     */
    @Nullable
    public <T extends WormTable> List<T> findAll() {
        return findAll(0, 0);
    }

    /**
     * Finds all rows and returns it.
     *
     * @param limit The limit of rows the result will contain.
     * @param <T> The type of the results.
     * @return {@code List} if the operation was successful, {@code null} if something went wrong.
     */
    @Nullable
    public <T extends WormTable> List<T> findAll(int limit) {
        return findAll(0, limit);
    }

    /**
     * Finds all rows and returns it.
     *
     * @param offset The offset the results will begin at.
     * @param limit The limit of rows the result will contain.
     * @param <T> The type of the results.
     * @return {@code List} if the operation was successful, {@code null} if something went wrong.
     */
    @Nullable
    public <T extends WormTable> List<T> findAll(int offset, int limit) {
        return findAllWhere(null, offset, limit);
    }

    /**
     * Finds all rows matching {@code whereClause} as the query's where clause and returns it.
     * {@code WormTable#newInstance} and {@code WormTable#newInstance(T)} should be implemented for this to work.
     *
     * @param whereClause The where clause.
     * @param <T> The type of the results.
     * @return {@code List} if the operation was successful, {@code null} if something went wrong.
     */
    @Nullable
    public<T extends WormTable> List<T> findAllWhere(String whereClause) {
        return this.findAllWhere(whereClause, 0, 0);
    }

    /**
     * Finds all rows matching {@code whereClause} as the query's where clause and returns it.
     * {@code WormTable#newInstance} and {@code WormTable#newInstance(T)} should be implemented for this to work.
     *
     * @param whereClause The where clause.
     * @param limit The limit of rows the result will contain.
     * @param <T> The type of the results.
     * @return {@code List} if the operation was successful, {@code null} if something went wrong.
     */
    @Nullable
    public <T extends WormTable> List<T> findAllWhere(String whereClause, int limit) {
        return this.findAllWhere(whereClause, 0, limit);
    }

    /**
     * Finds all rows matching {@code whereClause} as the query's where clause and returns it.
     * {@code WormTable#newInstance} and {@code WormTable#newInstance(T)} should be implemented for this to work.
     *
     * @param whereClause The where clause.
     * @param offset The offset the results will begin at.
     * @param limit The limit of rows the result will contain.
     * @param <T> The type of the results.
     * @return {@code List} if the operation was successful, {@code null} if something went wrong.
     */
    @SuppressWarnings("unchecked")
    @Nullable
    public <T extends WormTable> List<T> findAllWhere(String whereClause, int offset, int limit) {
        try {
            if (!findWhere(whereClause, offset, limit)) return null;

            List<T> results = limit > 0 ? new ArrayList<>(limit) : new ArrayList<>();

            T instance = (T) this.clone();
            while (next(instance))
                results.add((T) instance.clone());

            return results;
        } catch (Exception exception) {
            Worm.getLogger().error("Something went wrong while finding all rows in table " + tableMeta.getName(), exception);
            return null;
        }
    }
    //endregion

    //region Positional Find
    /**
     * Find and parses the first row based on the auto-increment column.
     *
     * @return If the operation was successful.
     */
    public boolean findFirst() {
        try {
            WormColumn column = tableMeta.getAutoIncrementColumn();
            Objects.requireNonNull(column, "The table must have one auto-increment primary key column");

            if (!findWhere("MIN(" + column.getSqlName() + ")", 0, 1)) return false;

            return next();
        } catch (Exception exception) {
            Worm.getLogger().error("Something went wrong while finding all rows in table " + tableMeta.getName(), exception);
            return false;
        }
    }

    /**
     * Find and parses the last row based on the auto-increment column.
     *
     * @return If the operation was successful.
     */
    public boolean findLast() {
        try {
            WormColumn column = tableMeta.getAutoIncrementColumn();
            Objects.requireNonNull(column, "The table must have one auto-increment primary key column");
            if (!findWhere("MAX(" + column.getSqlName() + ")", 0, 1)) return false;

            return next();
        } catch (Exception exception) {
            Worm.getLogger().error("Something went wrong while finding all rows in table " + tableMeta.getName(), exception);
            return false;
        }
    }
    //endregion

    @Override
    protected WormTable clone() throws CloneNotSupportedException {
        return (WormTable) super.clone();
    }

    @Override
    public void close() {
        try {
            if (this.currentQuery != null) this.currentQuery.close();
            this.currentQuery = null;
        } catch (SQLException exception) {
            Worm.getLogger().error("Something went wrong while closing table " + tableMeta.getName(), exception);
        }
    }

}
