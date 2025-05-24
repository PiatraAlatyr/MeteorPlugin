package ru.elementcraft.elementmeteor;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class MariaDbManager {
    private final HikariDataSource dataSource;

    public MariaDbManager(String host, int port, String database, String user, String password) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mariadb://" + host + ":" + port + "/" + database + "?useSSL=false&serverTimezone=UTC");
        config.setUsername(user);
        config.setPassword(password);
        config.setDriverClassName("org.mariadb.jdbc.Driver");
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(10000);
        config.setIdleTimeout(60000);
        config.setMaxLifetime(600000);
        this.dataSource = new HikariDataSource(config);
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public void close() {
        dataSource.close();
    }
}