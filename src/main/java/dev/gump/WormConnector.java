package dev.gump;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class WormConnector {
    static Connection connection;
    private static HikariDataSource hikariDataSource;
    private static WormConnection wormConnection;

    public static void init(WormConnection wormConnection){
        WormConnector.wormConnection = wormConnection;

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(wormConnection.getConnectionString());
        hikariConfig.setUsername(wormConnection.getUser());
        hikariConfig.setPassword(wormConnection.getPassword());
        hikariConfig.addDataSourceProperty("CachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("PrepStmtCacheSize", "250");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        hikariDataSource = new HikariDataSource(hikariConfig);
    }

    public static WormQuery Query(String sql) {
        if(wormConnection.isDebug())
            System.out.println("[SQL] " + sql);
        try {
            Connection connection = hikariDataSource.getConnection();
            return new WormQuery(connection, connection.prepareStatement(sql));
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return null;
    }
}
