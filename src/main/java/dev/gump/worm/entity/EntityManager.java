package dev.gump.worm.entity;

import dev.gump.worm.Worm;
import dev.gump.worm.WormQuery;
import dev.gump.worm.builders.WormQueryBuilder;
import dev.gump.worm.field.SequenceType;
import dev.gump.worm.field.WormField;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class EntityManager {
    public static <T extends Entity> CompletableFuture<T> insert(T entity){
        return CompletableFuture.supplyAsync(() -> {
            try {
                List<String> strQuery = WormQueryBuilder.insert(entity.getEntityMeta(), entity);
                WormField autoIncrementColumn = entity.getEntityMeta().getAutoIncrementColumn();

                WormQuery query = autoIncrementColumn == null ? Worm.query(strQuery.get(0)) : Worm.queryWithGeneratedKeys(strQuery.get(0));
                try (query) {
                    query.executeUpdate();

                    if (autoIncrementColumn != null) {
                        ResultSet generatedKeys = query.getStatement().getGeneratedKeys();

                        if (generatedKeys.next()) {
                            int generatedId = generatedKeys.getInt(1);
                            Field field = entity.getClass().getDeclaredField(autoIncrementColumn.getName());
                            field.setAccessible(true);
                            field.set(entity, generatedId);
                        }
                    }
                }
                query.close();

                int i = 1;
                for(WormField field : entity.getEntityMeta().getUniqueKeyColumns()){
                    if(field.getIncrement() != SequenceType.RANDOM_STRING && field.getIncrement() != SequenceType.RANDOM_UUID) continue;
                    Field f = entity.getClass().getDeclaredField(field.getFieldName());
                    f.setAccessible(true);
                    if(field.getIncrement() == SequenceType.RANDOM_UUID)
                        f.set(entity, UUID.fromString(strQuery.get(i)));
                    else
                        f.set(entity, strQuery.get(i));
                    i++;
                }

                return entity;
            } catch (Exception exception) {
                Worm.getLogger().error("Something went wrong while inserting table " + entity.getEntityMeta().getName(), exception);
                return null;
            }
        });
    }

    public static <T extends Entity> CompletableFuture<T> delete(T entity){
        return CompletableFuture.supplyAsync(() -> {
            try {
                StringBuilder sqlBuilder = new StringBuilder("DELETE FROM `").append(entity.getEntityMeta().getName()).append("`");

                sqlBuilder.append(" WHERE ").append(WormQueryBuilder.uniqueFieldsWhereClause(entity.getEntityMeta(), entity, false));

                try (WormQuery query = Worm.query(sqlBuilder.toString())) {
                    query.executeUpdate();
                }

                return entity;
            } catch (Exception exception) {
                Worm.getLogger().error("Something went wrong while deleting from table " + entity.getEntityMeta().getName(), exception);
                return null;
            }
        });
    }

    public static <T extends Entity> CompletableFuture<T> update(T entity){
        return update(entity, false);
    }
    public static <T extends Entity> CompletableFuture<T> update(T entity, boolean ignoreNull){
        return CompletableFuture.supplyAsync(() -> {
            try {
                HashMap<String, String> fields = EntityLoader.save(entity, entity.getEntityMeta());
                String sql = WormQueryBuilder.update(entity.getEntityMeta(), fields, ignoreNull);
                String where = WormQueryBuilder.uniqueFieldsWhereClause(entity.getEntityMeta(), entity, false);
                try (WormQuery query = Worm.query(sql + " WHERE " + where)) {
                    query.executeUpdate();
                    return entity;
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
