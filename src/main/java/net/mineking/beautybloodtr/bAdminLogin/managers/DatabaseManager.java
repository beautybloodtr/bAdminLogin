package net.mineking.beautybloodtr.bAdminLogin.managers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;

import org.bukkit.Bukkit;

public class DatabaseManager {

    private final String DATABASE_URL;

    public DatabaseManager(ConfigManager configManager) {
        DATABASE_URL = "jdbc:sqlite:plugins/bAdminLogin/bAdminLogin.db";
        initializeDatabase();
    }


    private void initializeDatabase() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            String createTable = "CREATE TABLE IF NOT EXISTS player_data ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "player_name TEXT NOT NULL,"
                    + "discord_id TEXT,"
                    + "password TEXT"
                    + ");";
            stmt.execute(createTable);
        } catch (SQLException e) {
            Bukkit.getLogger().log(Level.SEVERE, "Veritabanı oluşturulamadı!", e);
        }
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DATABASE_URL);
    }

    public void addPlayer(String playerName, String discordId, String password) {
        String sql = "INSERT INTO player_data (player_name, discord_id, password) VALUES (?, ?, ?)";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, playerName);
            pstmt.setString(2, discordId);
            pstmt.setString(3, password);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            Bukkit.getLogger().log(Level.SEVERE, "Kullanıcı eklenemedi!", e);
        }
    }

    public boolean playerExists(String playerName) {
        String sql = "SELECT COUNT(*) AS count FROM player_data WHERE player_name = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, playerName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("count") > 0;
            }
        } catch (SQLException e) {
            Bukkit.getLogger().log(Level.SEVERE, "Kullanıcı kontrol edilemedi!", e);
        }
        return false;
    }

    public void setPassword(String playerName, String newPassword) {
        String sql = "UPDATE player_data SET password = ? WHERE player_name = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newPassword);
            pstmt.setString(2, playerName);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            Bukkit.getLogger().log(Level.SEVERE, "Şifre güncellenemedi!", e);
        }
    }
    public boolean checkPassword(String playerName, String enteredPassword) {
        String sql = "SELECT password FROM player_data WHERE player_name = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, playerName);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String storedPassword = rs.getString("password");
                return storedPassword.equals(enteredPassword);
            }
        } catch (SQLException e) {
            Bukkit.getLogger().severe("Veritabanından şifre kontrolü sırasında hata oluştu: " + e.getMessage());
        }
        return false;
    }

    public void setDiscord(String playerName, String newDiscordId) {
        String sql = "UPDATE player_data SET discord_id = ? WHERE player_name = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newDiscordId);
            pstmt.setString(2, playerName);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            Bukkit.getLogger().log(Level.SEVERE, "Discord ID güncellenemedi!", e);
        }
    }

    public String getDiscordId(String playerName) {
        String sql = "SELECT discord_id FROM player_data WHERE player_name = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, playerName);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getString("discord_id");
            }
        } catch (SQLException e) {
            Bukkit.getLogger().severe("Veritabanından Discord ID alınırken hata oluştu: " + e.getMessage());
        }
        return null;
    }


    public boolean removePlayer(String identifier) {
        String sql = "DELETE FROM player_data WHERE player_name = ? OR discord_id = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, identifier);
            pstmt.setString(2, identifier);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            Bukkit.getLogger().log(Level.SEVERE, "Kullanıcı silinemedi!", e);
        }
        return false;
    }


    public void getPlayerData(String playerName) {
        String sql = "SELECT id, player_name, discord_id, password FROM player_data WHERE player_name = ?";

        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, playerName);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id");
                String discordId = rs.getString("discord_id");
                String password = rs.getString("password");
                Bukkit.getLogger().info("ID: " + id + ", Oyuncu İsmi: " + playerName + ", Discord ID: " + discordId + ", Şifre: " + password);
            }
        } catch (SQLException e) {
            Bukkit.getLogger().log(Level.SEVERE, "Oyuncu verileri alınamadı!", e);
        }
    }
}
