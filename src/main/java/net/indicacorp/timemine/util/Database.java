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

    public void initDatabase() {
        this.fetchConnection();
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
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.executeUpdate();
            plugin.getLogger().info("Database connected and initialized.");
        } catch (SQLException e) {
            plugin.getLogger().warning("There was a SQLException while initializing the database:");
            e.printStackTrace();
        } catch (Exception e) {
            if (!plugin.isEnabled()) return;
            plugin.getLogger().warning("There was a problem with the database connection:");
            e.printStackTrace();
        } finally {
            this.closeConnection();
        }
    }

    public ResultSet query(String sql) {
        this.fetchConnection();
        try {
            PreparedStatement stmt = connection.prepareStatement(sql);
            return stmt.executeQuery();
        } catch (SQLException e) {
            return null;
        }
    }

    public void insertOrUpdate(String sql) {
        this.fetchConnection();
        try {
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

    private void fetchConnection() {
        final FileConfiguration config = plugin.getConfig();
        String url = "jdbc:mysql://" + config.getString("mysql.host") + ":" + config.getString("mysql.port") + "/" + config.getString("mysql.database") + "?useSSL=false";

        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(url, config.getString("mysql.username"), config.getString("mysql.password"));
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Could not connect to database. Please make sure the provided credentials were correctly inputted into the config file.");
            plugin.disablePlugin();
        }
    }
}
