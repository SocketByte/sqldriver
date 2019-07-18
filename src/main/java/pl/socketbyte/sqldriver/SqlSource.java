package pl.socketbyte.sqldriver;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Class for abstracing HikariCP API from the rest
 * Handles the Hikari connection pool stuff
 */
public class SqlSource {

    /**
     * HikariDataSource which is the heart of Hikari connection pool
     */
    private final HikariDataSource source;

    /**
     * SqlSource uses properties file to initialize SQL credentials
     * @param propertiesPath properties file with SQL credentials, can be classpath or filesystem path
     */
    public SqlSource(String propertiesPath) {
        HikariConfig config = new HikariConfig(propertiesPath);
        config.setPoolName("SqlDriverPool");

        this.source = new HikariDataSource(config);
    }

    /**
     * Borrows the connection from the connection pool, must be closed after use
     * @return Connection
     */
    public Connection borrow() {
        try {
            return this.source.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Closes the HikariDataSource
     */
    public void close() {
        this.source.close();
    }

}
