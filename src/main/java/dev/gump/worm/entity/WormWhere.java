package dev.gump.worm.entity;

import dev.gump.worm.Worm;
import dev.gump.worm.WormQuery;
import dev.gump.worm.WormUtils;
import dev.gump.worm.builders.Order;
import dev.gump.worm.builders.WormOperation;
import dev.gump.worm.builders.WormQueryBuilder;
import dev.gump.worm.builders.WormWhereBuilder;
import dev.gump.worm.field.WormField;

import javax.management.Query;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class WormWhere{
    private final EntityMeta meta;
    private List<String> fields = new ArrayList<>();
    private List<WormOperation> where = new ArrayList<>();

    private String orderBy = null;
    private String group = null;
    private String limit = null;
    private String offset = null;
    private String context = null;
    private WormQueryType type = WormQueryType.NONE;

    public WormWhere(EntityMeta meta){
        this.meta = meta;
    }

    /**
     * select the fields in the query
     *
     * @param fields the context name
     */
    public WormWhere field(String... fields){
        this.fields.addAll(List.of(fields));
        return this;
    }

    /**
     * select a context for this query
     *
     * @param context the context name
     */
    public WormWhere context(String context){
        this.context = context;
        return this;
    }

    /**
     * select an limit to query
     *
     * @param limit the limit of results
     */
    public WormWhere limit(int limit){
        this.limit = "LIMIT " + limit;
        return this;
    }

    /**
     * select an limit to query
     *
     * @param limit the limit of results
     * @param offset the offset of results
     */
    public WormWhere limit(int limit, int offset){
        this.limit = "LIMIT " + limit + " OFFSET " + offset;
        this.offset = null;
        return this;
    }

    /**
     * select an offset to query
     *
     * @param offset the offset of results
     */
    public WormWhere offset(int offset){
        this.offset = "OFFSET " + offset;
        return this;
    }

    /**
     * select an order to query
     *
     * @param field the fields going to be ordered by
     */
    public WormWhere orderBy(String... field){
        orderBy = WormWhereBuilder.order(meta, Order.ASC);
        return this;
    }

    /**
     * select an order to query
     *
     * @param order the ASC or DESC order
     * @param field the fields going to be ordered by
     */
    public WormWhere orderBy(Order order, String... field){
        orderBy = WormWhereBuilder.order(meta, order, field);
        return this;
    }

    /**
     * select an group to query
     *
     * @param field the fields going to be grouped by
     */
    public WormWhere groupBy(String... field){
        group = WormWhereBuilder.group(meta, field);
        return this;
    }

    /**
     * add a where to the query
     *
     * @param operations the where operations
     */
    public WormWhere where(WormOperation... operations){
        where.addAll(List.of(operations));
        return this;
    }

    /**
     * find a unique result in database
     *
     * @param id the unique values on database
     * @return a class that extends Entity
     */
    public <T extends Entity> CompletableFuture<T> findUnique(Object... id){
        type = WormQueryType.GET;

        return CompletableFuture.supplyAsync(() -> {
            String sql = WormWhereBuilder.queryWithId(meta, this, id);
            try {
                WormQuery query = Worm.query(sql);
                ResultSet resultSet = query.executeQuery();
                T result = null;
                if (resultSet.next()) {
                    result = (T) EntityLoader.load(meta.getEntityClass(), resultSet, meta);
                }
                query.close();
                return result;
            } catch (SQLException | NoSuchFieldException | IllegalAccessException | InstantiationException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * find many result in database
     *
     * @return a list of class that extends Entity
     */
    public <T extends Entity> CompletableFuture<List<T>> find(){
        type = WormQueryType.FIND;

        return CompletableFuture.supplyAsync(() -> {
            String sql = WormWhereBuilder.queryWithWhere(meta, this);
            List<T> result = new ArrayList<>();
            try {
                WormQuery query = Worm.query(sql);
                ResultSet resultSet = query.executeQuery();
                while (resultSet.next()) {
                    result.add((T) EntityLoader.load(meta.getEntityClass(), resultSet, meta));
                }
                query.close();
                return result;
            } catch (SQLException | NoSuchFieldException | IllegalAccessException | InstantiationException e) {
                throw new RuntimeException(e);
            }
        });
    }
    /**
     * find one result in database
     *
     * @return a class that extends Entity
     */
    public <T extends Entity> CompletableFuture<T> findOne(){
        type = WormQueryType.FIND_ONE;

        return CompletableFuture.supplyAsync(() -> {
            String sql = WormWhereBuilder.queryWithWhere(meta, this);
            try {
                WormQuery query = Worm.query(sql);
                ResultSet resultSet = query.executeQuery();
                T result = null;
                if (resultSet.next()) {
                    result = (T) EntityLoader.load(meta.getEntityClass(), resultSet, meta);
                }
                query.close();
                return result;
            } catch (SQLException | NoSuchFieldException | IllegalAccessException | InstantiationException e) {
                throw new RuntimeException(e);
            }
        });
    }
    /**
     * count the results of database
     *
     * @return a number of result from database
     */
    public CompletableFuture<Integer> count(){
        type = WormQueryType.COUNT;

        return CompletableFuture.supplyAsync(() -> {
            String sql = WormWhereBuilder.queryWithWhere(meta, this);
            try {
                WormQuery query = Worm.query(sql);
                ResultSet resultSet = query.executeQuery();
                int result = 0;
                if (resultSet.next())
                    result = resultSet.getInt("count");
                query.close();
                return result;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }
    /**
     * update many rows in database
     *
     * @param entity the entity sample that going to be value of all
     */
    public void update(Entity entity){
        try {
            HashMap<String, String> fields = EntityLoader.save(entity, meta);
            String sql = WormQueryBuilder.update(meta, fields, true);
            String where = WormWhereBuilder.onlyWhere(this);
            try (WormQuery query = Worm.query(sql + " " + where)) {
                query.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
    /**
     * delete many rows in database
     */
    public void delete(){
        String sql = "DELETE FROM " + meta.getName();
        String where = WormWhereBuilder.onlyWhere(this);
        try (WormQuery query = Worm.query(sql + " " + where)) {
            query.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public EntityMeta getMeta() {
        return meta;
    }

    public List<WormOperation> getWhere() {
        return where;
    }

    public String getGroup() {
        return group;
    }

    public String getLimit() {
        return limit;
    }

    public String getOrderBy() {
        return orderBy;
    }

    public String getOffset() {
        return offset;
    }

    public WormQueryType getType() {
        return type;
    }

    public String getContext() {
        return context;
    }

    public List<String> getFields(){
        return fields;
    }
}
