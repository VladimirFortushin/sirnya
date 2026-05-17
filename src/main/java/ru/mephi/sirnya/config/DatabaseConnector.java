package ru.mephi.sirnya.config;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.io.InputStream;

public class DatabaseConnector {
    private final String url;
    private final String username;
    private final String password;

    public DatabaseConnector() {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("db.properties")) {
            Properties props = new Properties();
            props.load(is);
            this.url = props.getProperty("db.url");
            this.username = props.getProperty("db.username");
            this.password = props.getProperty("db.password");
            Class.forName(props.getProperty("db.driver"));
        } catch (Exception e) {
            throw new RuntimeException("Failed to load DB configuration", e);
        }
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }
}
