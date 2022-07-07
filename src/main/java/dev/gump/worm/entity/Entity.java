package dev.gump.worm.entity;

import dev.gump.worm.Worm;

import java.lang.invoke.MethodHandles;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

public class Entity {
    private final EntityMeta entityMeta;

    public Entity(){
        entityMeta = Worm.getRegistry().getTableMeta(getClass());
    }

    /**
     * Get the entity Meta
     */
    public EntityMeta getEntityMeta() {
        return entityMeta;
    }

    /**
     * Insert that class in database
     *
     * @return the CompletableFuture about that insertion
     */
    public <T extends Entity> CompletableFuture<T> insert(){
        return (CompletableFuture<T>) EntityManager.insert(this);
    }

    /**
     * update that class in database
     *
     * @param ignoreNull if is true won't update null values
     * @return the CompletableFuture about that query
     */
    public <T extends Entity> CompletableFuture<T> update(boolean ignoreNull){
        return (CompletableFuture<T>) EntityManager.update(this, ignoreNull);
    }
    /**
     * update that class in database
     *
     * @return the CompletableFuture about that query
     */
    public <T extends Entity> CompletableFuture<T> update(){
        return (CompletableFuture<T>) EntityManager.update(this);
    }

    /**
     * delete that class in database
     *
     * @return the CompletableFuture about that query
     */
    public <T extends Entity> CompletableFuture<T> delete(){
        return (CompletableFuture<T>) EntityManager.delete(this);
    }
}
