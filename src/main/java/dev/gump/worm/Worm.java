package dev.gump.worm;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dev.gump.worm.builders.WormQueryBuilder;
import dev.gump.worm.entity.Entity;
import dev.gump.worm.entity.EntityMeta;
import dev.gump.worm.entity.WormWhere;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

public class Worm {
    private static final Worm instance = new Worm();
    private static final Logger logger = LoggerFactory.getLogger("Worm Logger");

    private final WormRegistry wormRegistry;
    private final WormTypeAdapterRegistry wormTypeAdapterRegistry;
    private final ExecutorService executor;
    private HikariDataSource hikariDataSource;
    private boolean isDebug;

    private Worm() {
        this.wormRegistry = new WormRegistry();
        this.wormTypeAdapterRegistry = new WormTypeAdapterRegistry();
        this.executor = Executors.newCachedThreadPool();
    }

    /**
     * Intitialize the Worm in your project
     *
     * @param wormConnection The connection to database.
     */
    public static void initialize(WormConnection wormConnection) {
        initialize(wormConnection, null);
    }

    /**
     * Intitialize the Worm in your project
     *
     * @param wormConnection The connection to database.
     * @param onCreated Callback when the Worm is initialized.
     */
    public static void initialize(WormConnection wormConnection, Runnable onCreated) {
        instance.isDebug = wormConnection.isDebug();

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(wormConnection.getUrl());
        hikariConfig.setUsername(wormConnection.getUser());
        hikariConfig.setPassword(wormConnection.getPassword());
        hikariConfig.addDataSourceProperty("CachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("PrepStmtCacheSize", "250");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        hikariConfig.setMaximumPoolSize(wormConnection.getMaxPoolSize());
        hikariConfig.setMaxLifetime(wormConnection.getMaxLifetime());
        hikariConfig.setLeakDetectionThreshold(wormConnection.getLeakDetectionThreshold());
        hikariConfig.setMinimumIdle(wormConnection.getMinPoolSize());
        instance.hikariDataSource = new HikariDataSource(hikariConfig);

        runAsync(() -> {
            for (EntityMeta tableMeta : instance.wormRegistry.getTables()) {
                String strQuery = WormQueryBuilder.createTable(tableMeta);
                try (WormQuery query = Worm.query(strQuery)) {
                    query.executeUpdate();
                } catch (SQLException exception) {
                    logger.error("Something went wrong while creating table " + tableMeta.getName(), exception);
                    break;
                }
            }
        }).thenRunAsync(() -> {
            logger.info("All worm tables were created!");
            if (onCreated != null)
                onCreated.run();
        }, instance.executor);
    }

    /**
     * Get the logger from Worm
     */
    public static Logger getLogger() {
        return logger;
    }

    /**
     * Get the registry from Worm
     */
    public static WormRegistry getRegistry() {
        return instance.wormRegistry;
    }

    /**
     * Get the TypeAdapter registry from Worm
     */
    public static WormTypeAdapterRegistry getTypeAdapterRegistry() {
        return instance.wormTypeAdapterRegistry;
    }

    /**
     * Make a query using Worm
     *
     * @param sql SQL String to query
     * @return WormQuery object, with Connection and ResultSet
     */
    public static WormQuery query(String sql) throws SQLException {
        if(instance.isDebug)
            logger.info("[SQL] " + sql);

        Connection connection = instance.hikariDataSource.getConnection();
        return new WormQuery(connection, connection.prepareStatement(sql));
    }

    /**
     * Make a query using Worm and return the generated keys
     *
     * @param sql SQL String to query
     * @return WormQuery object, with Connection and ResultSet
     */
    public static WormQuery queryWithGeneratedKeys(String sql) throws SQLException {
        if(instance.isDebug)
            logger.info("[SQL] " + sql);

        Connection connection = instance.hikariDataSource.getConnection();
        return new WormQuery(connection, connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS));
    }

    /**
     * Run async
     */
    public static CompletableFuture<Void> runAsync(Runnable runnable) {
        return CompletableFuture.runAsync(runnable, instance.executor);
    }

    /**
     * Run async and return
     */
    public static <T> CompletableFuture<T> supplyAsync(Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(supplier, instance.executor);
    }

    /**
     * Select table from class
     */
    public static WormWhere of(Class<? extends Entity> table){
        return new WormWhere(getRegistry().getTableMeta(table));
    }

    /**
     * Stop worm
     */
    public static void stop() {
        instance.hikariDataSource.close();
        instance.executor.shutdown();
    }
}
