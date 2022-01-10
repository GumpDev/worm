package dev.gump.worm;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class WormConnector {
    private static HikariDataSource hikariDataSource;
    private static boolean isDebug;

    public static void init(WormConnection wormConnection) {
        WormConnector.isDebug = wormConnection.isDebug();

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(wormConnection.getUrl());
        hikariConfig.setUsername(wormConnection.getUser());
        hikariConfig.setPassword(wormConnection.getPassword());
        hikariConfig.addDataSourceProperty("CachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("PrepStmtCacheSize", "250");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        hikariDataSource = new HikariDataSource(hikariConfig);
    }

    public static WormQuery Query(String sql) throws SQLException {
        if(isDebug)
            System.out.println("[SQL] " + sql);

        Connection connection = hikariDataSource.getConnection();
        return new WormQuery(connection, connection.prepareStatement(sql));
    }

    public static WormQuery QueryWithGeneratedKeys(String sql) throws SQLException {
        if(isDebug)
            System.out.println("[SQL] " + sql);

        Connection connection = hikariDataSource.getConnection();
        return new WormQuery(connection, connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS));
    }

    public static void Close() {
        hikariDataSource.close();
    }
}
