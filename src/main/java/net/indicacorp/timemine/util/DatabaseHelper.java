package net.indicacorp.timemine.util;

import com.zaxxer.hikari.HikariDataSource;
import net.indicacorp.timemine.TimeMine;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.*;
import java.util.concurrent.CompletableFuture;

public class DatabaseHelper {

    private static HikariDataSource ds = new HikariDataSource();
    static TimeMine plugin = TimeMine.getInstance();

    static {
        FileConfiguration plConfig = plugin.getConfig();
        String url = "jdbc:mysql://" + plConfig.getString("mysql.host") + ":" + plConfig.getString("mysql.port") + "/" + plConfig.getString("mysql.database") + "?useSSL=false";
        ds.setJdbcUrl(url);
        ds.setUsername(plConfig.getString("mysql.username"));
        ds.setPassword(plConfig.getString("mysql.password"));
        ds.addDataSourceProperty("cachePrepStmts", "true");
        ds.addDataSourceProperty("prepStmtCacheSize", "250");
        ds.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        ds.setLeakDetectionThreshold(60 * 1000);
    }

    public static Connection getConnection() throws SQLException {
        return ds.getConnection();
    }

    public static void initDatabase() throws ClassNotFoundException, SQLException {
        /*
            Column         Default
            id             AUTO_INCREMENT
            x, y, z        -
            isMined        0
            displayBlock   -
            originalBlock  SMOOTH_STONE
            dropItem       -
            dropItemCount  1
            minedAt        NULL
            resetInterval  60
         */

        Class.forName("com.mysql.jdbc.Driver");

//        String sql = "DROP TABLE IF EXISTS timemine";
//        insertOrUpdateSync(sql);

        String sql = "CREATE TABLE IF NOT EXISTS timemine ( id BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT, x INT NOT NULL, y INT NOT NULL, z INT NOT NULL, world VARCHAR(255) NOT NULL, isMined TINYINT(1) NOT NULL DEFAULT 0, displayBlock VARCHAR(50) NOT NULL, originalBlock VARCHAR(50) NOT NULL DEFAULT 'SMOOTH_STONE', dropItem VARCHAR(50) NOT NULL, dropItemCount INT NOT NULL DEFAULT 1, minedAt TIMESTAMP NULL DEFAULT NULL, resetInterval INT NOT NULL DEFAULT 60)";
        long t = insertOrUpdateSync(sql);
        if (t == -1) throw new SQLException("Database couldn't be initialized.");

        plugin.getLogger().info("Database connected and initialized");
    }

    public static ResultSet query(String sql) {
        try {
            Connection connection = getConnection();
            PreparedStatement stmt = connection.prepareStatement(sql);
            return stmt.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void insertOrUpdateAsync(String sql) {
        CompletableFuture.runAsync(() -> {
            try {
                Connection connection = getConnection();
                PreparedStatement stmt = connection.prepareStatement(sql);
                stmt.executeUpdate();
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public static long insertOrUpdateSync(String sql) {
        long generatedKey = -1;
        try {
            Connection connection = getConnection();
            PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.executeUpdate();
            ResultSet generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                generatedKey = generatedKeys.getInt(1);
            } else {
                generatedKey = 0;
            }
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return generatedKey;
    }
}
