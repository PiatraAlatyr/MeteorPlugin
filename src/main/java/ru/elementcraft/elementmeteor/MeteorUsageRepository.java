package ru.elementcraft.elementmeteor;

import java.sql.*;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class MeteorUsageRepository {
    private final MariaDbManager db;

    public MeteorUsageRepository(MariaDbManager db) {
        this.db = db;
        CompletableFuture.runAsync(this::createTableIfNotExists);
    }

    private void createTableIfNotExists() {
        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS meteor_usages (" +
                            "uuid VARCHAR(36) PRIMARY KEY," +
                            "usages INT NOT NULL" +
                            ")"
            );
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public CompletableFuture<Integer> getUsages(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = db.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                         "SELECT usages FROM meteor_usages WHERE uuid = ?")) {
                ps.setString(1, uuid.toString());
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    return rs.getInt("usages");
                } else {
                    return 0;
                }
            } catch (SQLException e) {
                e.printStackTrace();
                return 0;
            }
        });
    }

    public CompletableFuture<Integer> incrementUsages(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = db.getConnection()) {
                int usages = 0;
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT usages FROM meteor_usages WHERE uuid = ?")) {
                    ps.setString(1, uuid.toString());
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) usages = rs.getInt("usages");
                }
                usages++;
                try (PreparedStatement ps = conn.prepareStatement(
                        "REPLACE INTO meteor_usages (uuid, usages) VALUES (?, ?)")) {
                    ps.setString(1, uuid.toString());
                    ps.setInt(2, usages);
                    ps.executeUpdate();
                }
                return usages;
            } catch (SQLException e) {
                e.printStackTrace();
                return 1;
            }
        });
    }
}