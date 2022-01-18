package dev.gump.worm;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
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

    public static void init(WormConnection wormConnection) {
        instance.isDebug = wormConnection.isDebug();

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(wormConnection.getUrl());
        hikariConfig.setUsername(wormConnection.getUser());
        hikariConfig.setPassword(wormConnection.getPassword());
        hikariConfig.addDataSourceProperty("CachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("PrepStmtCacheSize", "250");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        instance.hikariDataSource = new HikariDataSource(hikariConfig);

        runAsync(() -> {
            for (WormTableMeta tableMeta : instance.wormRegistry.getTables()) {
                String strQuery = WormQueryBuilders.buildCreateTableQuery(tableMeta);
                try (WormQuery query = Worm.query(strQuery)) {
                    query.executeUpdate();
                } catch (SQLException exception) {
                    logger.error("Something went wrong while creating table " + tableMeta.getName(), exception);
                    break;
                }
            }
        }).thenRun(() -> logger.info("All worm tables were created!"));
    }

    public static Logger getLogger() {
        return logger;
    }

    public static WormRegistry getRegistry() {
        return instance.wormRegistry;
    }

    public static WormTypeAdapterRegistry getTypeAdapterRegistry() {
        return instance.wormTypeAdapterRegistry;
    }

    public static WormQuery query(String sql) throws SQLException {
        if(instance.isDebug)
            logger.info("[SQL] " + sql);

        Connection connection = instance.hikariDataSource.getConnection();
        return new WormQuery(connection, connection.prepareStatement(sql));
    }

    public static WormQuery queryWithGeneratedKeys(String sql) throws SQLException {
        if(instance.isDebug)
            logger.info("[SQL] " + sql);

        Connection connection = instance.hikariDataSource.getConnection();
        return new WormQuery(connection, connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS));
    }

    public static CompletableFuture<Void> runAsync(Runnable runnable) {
        return CompletableFuture.runAsync(runnable, instance.executor);
    }

    public static <T> CompletableFuture<T> supplyAsync(Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(supplier, instance.executor);
    }

    public static void stop() {
        instance.hikariDataSource.close();
        instance.executor.shutdown();
    }

}
