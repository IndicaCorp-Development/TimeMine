package net.indicacorp.timemine.util;

import net.indicacorp.timemine.TimeMine;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.*;

public class Database {

    TimeMine plugin;

    public Database() {
        plugin = TimeMine.getInstance();
    }

    private Connection connection = null;

    public boolean checkDatabaseDriver() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public void initDatabase() throws Exception {
        /*
        Column         Default
        x, y, z        -
        isMined        0
        displayBlock   -
        originalBlock  SMOOTH_STONE
        dropItem       -
        dropItemCount  1
        minedAt        NULL
        resetInterval  60
         */
        String sql = "CREATE TABLE IF NOT EXISTS timemine ( x INT NOT NULL, y INT NOT NULL, z INT NOT NULL, world VARCHAR(255) NOT NULL, isMined TINYINT(1) NOT NULL DEFAULT 0, displayBlock VARCHAR(50) NOT NULL, originalBlock VARCHAR(50) NOT NULL DEFAULT 'SMOOTH_STONE', dropItem VARCHAR(50) NOT NULL, dropItemCount INT NOT NULL DEFAULT 1, minedAt TIMESTAMP NULL DEFAULT NULL, resetInterval INT NOT NULL DEFAULT 60)";

        this.fetchConnection();
        PreparedStatement stmt = connection.prepareStatement(sql);
        stmt.executeUpdate();
        plugin.getLogger().info("Database connected and initialized.");
    }

    public ResultSet query(String sql) {
        try {
            this.fetchConnection();
            PreparedStatement stmt = connection.prepareStatement(sql);
            return stmt.executeQuery();
        } catch (SQLException e) {
            return null;
        } catch (Exception e) {
            this.closeConnection();
            return null;
        }
    }

    public void insertOrUpdate(String sql) {
        try {
            this.fetchConnection();
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
            this.closeConnection();
        }
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void fetchConnection() throws Exception {
        final FileConfiguration config = plugin.getConfig();
        String url = "jdbc:mysql://" + config.getString("mysql.host") + ":" + config.getString("mysql.port") + "/" + config.getString("mysql.database") + "?useSSL=false";

        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(url, config.getString("mysql.username"), config.getString("mysql.password"));
        }
    }
}
