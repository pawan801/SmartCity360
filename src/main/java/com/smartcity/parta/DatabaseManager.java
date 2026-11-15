package com.smartcity.parta;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DatabaseManager {

    private static final String DB_URL = "jdbc:h2:./smartcity360_db;DB_CLOSE_DELAY=-1";
    private static final String DB_USER = "sa";
    private static final String DB_PASS = "";

    static {
        try {
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("H2 Driver not found: " + e.getMessage());
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
    }

    public static synchronized void initDatabase() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            // 1. Sensor Telemetry Logs (Part A - Decorator)
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS sensor_telemetry_logs (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "raw_data VARCHAR(500) NOT NULL, " +
                    "decorators_applied VARCHAR(255) NOT NULL, " +
                    "processed_output VARCHAR(1000) NOT NULL, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

            // 2. Citizen Grievance Tickets (Part C - Chain of Responsibility)
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS grievance_tickets (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "complaint_id VARCHAR(50) NOT NULL, " +
                    "citizen_name VARCHAR(255) NOT NULL, " +
                    "description VARCHAR(1000) NOT NULL, " +
                    "severity VARCHAR(50) NOT NULL, " +
                    "resolved_by VARCHAR(100) NOT NULL, " +
                    "resolution_details VARCHAR(1000) NOT NULL, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

            // 3. Registered Streetlights (Part D - Flyweight)
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS streetlights (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "light_id VARCHAR(50) NOT NULL, " +
                    "model VARCHAR(100) NOT NULL, " +
                    "wattage INT NOT NULL, " +
                    "color_temp VARCHAR(50) NOT NULL, " +
                    "latitude DOUBLE NOT NULL, " +
                    "longitude DOUBLE NOT NULL, " +
                    "brightness INT NOT NULL, " +
                    "shared_instance_hash VARCHAR(100) NOT NULL, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

            seedInitialDataIfEmpty(conn);

            System.out.println("[DatabaseManager] H2 Embedded Database initialized successfully (" + DB_URL + ")");
        } catch (SQLException e) {
            System.err.println("[DatabaseManager] Initialization error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void seedInitialDataIfEmpty(Connection conn) throws SQLException {
        // Seed Telemetry if empty
        try (Statement s = conn.createStatement();
             ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM sensor_telemetry_logs")) {
            if (rs.next() && rs.getInt(1) == 0) {
                insertTelemetryLogDirect(conn, "Voltage: 230V, Usage: 45.8kWh", "Encryption, Authentication, Compression",
                        "Raw Energy Data : DataStream Data Encrypted With Data Authentication With Data Compression");
                insertTelemetryLogDirect(conn, "Voltage: 220V, Usage: 38.2kWh", "Encryption",
                        "Raw Energy Data : DataStream Data Encrypted");
                insertTelemetryLogDirect(conn, "Voltage: 240V, Usage: 52.0kWh", "Compression",
                        "Raw Energy Data : DataStream Data Compressed With Data Compression");
            }
        }

        // Seed Grievances if empty
        try (Statement s = conn.createStatement();
             ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM grievance_tickets")) {
            if (rs.next() && rs.getInt(1) == 0) {
                insertGrievanceDirect(conn, "GRV-001", "Kamal Perera", "Streetlight blinking near main gate", "LOW",
                        "Frontline Desk", "[Frontline Desk] Resolved complaint GRV-001 for Kamal Perera: Streetlight blinking near main gate");
                insertGrievanceDirect(conn, "GRV-002", "Sunil Silva", "Sewage blockage overflow in residential sector", "MEDIUM",
                        "Department Supervisor", "[Supervisor] Resolved operational issue GRV-002 for Sunil Silva: Sewage blockage overflow in residential sector");
                insertGrievanceDirect(conn, "GRV-003", "Nimal Fernando", "Industrial gas leak alarm triggered near school", "HIGH",
                        "Municipal Manager", "[Municipal Manager] Resolved critical hazard GRV-003 for Nimal Fernando: Industrial gas leak alarm triggered near school");
            }
        }

        // Seed Streetlights if empty
        try (Statement s = conn.createStatement();
             ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM streetlights")) {
            if (rs.next() && rs.getInt(1) == 0) {
                SharedStreetlight s1 = SharedStreetlightPool.getSharedStreetlight("LED-LUMEN-60W", 60, "4000K");
                SharedStreetlight s2 = SharedStreetlightPool.getSharedStreetlight("HIGH-MAST-150W", 150, "5000K");

                insertStreetlightDirect(conn, "SL-COL-001", "LED-LUMEN-60W", 60, "4000K", 6.9271, 79.8612, 80,
                        "Shared@" + Integer.toHexString(System.identityHashCode(s1)));
                insertStreetlightDirect(conn, "SL-COL-002", "HIGH-MAST-150W", 150, "5000K", 6.9275, 79.8615, 100,
                        "Shared@" + Integer.toHexString(System.identityHashCode(s2)));
                insertStreetlightDirect(conn, "SL-COL-003", "LED-LUMEN-60W", 60, "4000K", 6.9280, 79.8620, 60,
                        "Shared@" + Integer.toHexString(System.identityHashCode(s1)));
                insertStreetlightDirect(conn, "SL-COL-004", "LED-LUMEN-60W", 60, "4000K", 6.9285, 79.8625, 90,
                        "Shared@" + Integer.toHexString(System.identityHashCode(s1)));
            }
        }
    }

    private static void insertTelemetryLogDirect(Connection conn, String rawData, String decorators, String output) throws SQLException {
        String sql = "INSERT INTO sensor_telemetry_logs (raw_data, decorators_applied, processed_output) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, rawData);
            ps.setString(2, decorators);
            ps.setString(3, output);
            ps.executeUpdate();
        }
    }

    private static void insertGrievanceDirect(Connection conn, String id, String citizen, String desc, String severity, String resolver, String details) throws SQLException {
        String sql = "INSERT INTO grievance_tickets (complaint_id, citizen_name, description, severity, resolved_by, resolution_details) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.setString(2, citizen);
            ps.setString(3, desc);
            ps.setString(4, severity);
            ps.setString(5, resolver);
            ps.setString(6, details);
            ps.executeUpdate();
        }
    }

    private static void insertStreetlightDirect(Connection conn, String id, String model, int wattage, String colorTemp, double lat, double lng, int brightness, String hash) throws SQLException {
        String sql = "INSERT INTO streetlights (light_id, model, wattage, color_temp, latitude, longitude, brightness, shared_instance_hash) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.setString(2, model);
            ps.setInt(3, wattage);
            ps.setString(4, colorTemp);
            ps.setDouble(5, lat);
            ps.setDouble(6, lng);
            ps.setInt(7, brightness);
            ps.setString(8, hash);
            ps.executeUpdate();
        }
    }

    // ==========================================
    // Public CRUD API Methods
    // ==========================================

    public static synchronized void saveTelemetryLog(String rawData, String decorators, String processedOutput) {
        String sql = "INSERT INTO sensor_telemetry_logs (raw_data, decorators_applied, processed_output) VALUES (?, ?, ?)";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, rawData);
            ps.setString(2, decorators);
            ps.setString(3, processedOutput);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[DatabaseManager] Error saving telemetry: " + e.getMessage());
        }
    }

    public static synchronized List<Map<String, Object>> getTelemetryLogs() {
        List<Map<String, Object>> logs = new ArrayList<>();
        String sql = "SELECT id, raw_data, decorators_applied, processed_output, created_at FROM sensor_telemetry_logs ORDER BY id DESC";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("id", rs.getInt("id"));
                row.put("rawData", rs.getString("raw_data"));
                row.put("decoratorsApplied", rs.getString("decorators_applied"));
                row.put("processedOutput", rs.getString("processed_output"));
                row.put("createdAt", rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toString() : "");
                logs.add(row);
            }
        } catch (SQLException e) {
            System.err.println("[DatabaseManager] Error fetching telemetry: " + e.getMessage());
        }
        return logs;
    }

    public static synchronized void saveGrievance(String complaintId, String citizenName, String description,
                                                 String severity, String resolvedBy, String resolutionDetails) {
        String sql = "INSERT INTO grievance_tickets (complaint_id, citizen_name, description, severity, resolved_by, resolution_details) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, complaintId);
            ps.setString(2, citizenName);
            ps.setString(3, description);
            ps.setString(4, severity);
            ps.setString(5, resolvedBy);
            ps.setString(6, resolutionDetails);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[DatabaseManager] Error saving grievance: " + e.getMessage());
        }
    }

    public static synchronized List<Map<String, Object>> getGrievanceTickets() {
        List<Map<String, Object>> tickets = new ArrayList<>();
        String sql = "SELECT id, complaint_id, citizen_name, description, severity, resolved_by, resolution_details, created_at FROM grievance_tickets ORDER BY id DESC";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("id", rs.getInt("id"));
                row.put("complaintId", rs.getString("complaint_id"));
                row.put("citizenName", rs.getString("citizen_name"));
                row.put("description", rs.getString("description"));
                row.put("severity", rs.getString("severity"));
                row.put("resolvedBy", rs.getString("resolved_by"));
                row.put("resolutionDetails", rs.getString("resolution_details"));
                row.put("createdAt", rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toString() : "");
                tickets.add(row);
            }
        } catch (SQLException e) {
            System.err.println("[DatabaseManager] Error fetching grievances: " + e.getMessage());
        }
        return tickets;
    }

    public static synchronized void saveStreetlight(String lightId, String model, int wattage, String colorTemp,
                                                   double latitude, double longitude, int brightness, String sharedHash) {
        String sql = "INSERT INTO streetlights (light_id, model, wattage, color_temp, latitude, longitude, brightness, shared_instance_hash) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, lightId);
            ps.setString(2, model);
            ps.setInt(3, wattage);
            ps.setString(4, colorTemp);
            ps.setDouble(5, latitude);
            ps.setDouble(6, longitude);
            ps.setInt(7, brightness);
            ps.setString(8, sharedHash);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[DatabaseManager] Error saving streetlight: " + e.getMessage());
        }
    }

    public static synchronized List<Map<String, Object>> getStreetlights() {
        List<Map<String, Object>> lights = new ArrayList<>();
        String sql = "SELECT id, light_id, model, wattage, color_temp, latitude, longitude, brightness, shared_instance_hash, created_at FROM streetlights ORDER BY id DESC";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("id", rs.getInt("id"));
                row.put("lightId", rs.getString("light_id"));
                row.put("model", rs.getString("model"));
                row.put("wattage", rs.getInt("wattage"));
                row.put("colorTemp", rs.getString("color_temp"));
                row.put("latitude", rs.getDouble("latitude"));
                row.put("longitude", rs.getDouble("longitude"));
                row.put("brightness", rs.getInt("brightness"));
                row.put("sharedHash", rs.getString("shared_instance_hash"));
                row.put("createdAt", rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toString() : "");
                lights.add(row);
            }
        } catch (SQLException e) {
            System.err.println("[DatabaseManager] Error fetching streetlights: " + e.getMessage());
        }
        return lights;
    }

    public static synchronized Map<String, Object> getFlyweightMetrics() {
        Map<String, Object> metrics = new LinkedHashMap<>();
        int totalStreetlights = 0;
        int distinctSharedInstances = SharedStreetlightPool.getPoolSize();

        String countSql = "SELECT COUNT(*) FROM streetlights";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(countSql)) {
            if (rs.next()) {
                totalStreetlights = rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("[DatabaseManager] Error getting metrics: " + e.getMessage());
        }

        // Memory estimation calculation:
        // Intrinsic state (model string, wattage int, colorTemp string) ~ 96 bytes
        // Extrinsic state (id, lat, long, brightness, pointer) ~ 64 bytes
        long unsharedEstimatedBytes = (long) totalStreetlights * (96 + 64);
        long sharedEstimatedBytes = ((long) distinctSharedInstances * 96) + ((long) totalStreetlights * 64);
        double memorySavedPercent = 0.0;
        if (unsharedEstimatedBytes > 0) {
            memorySavedPercent = ((double) (unsharedEstimatedBytes - sharedEstimatedBytes) / unsharedEstimatedBytes) * 100.0;
            if (memorySavedPercent < 0) memorySavedPercent = 0.0;
        }

        metrics.put("totalStreetlights", totalStreetlights);
        metrics.put("distinctSharedInstances", distinctSharedInstances);
        metrics.put("unsharedEstimatedBytes", unsharedEstimatedBytes);
        metrics.put("sharedEstimatedBytes", sharedEstimatedBytes);
        metrics.put("memorySavedPercent", Math.round(memorySavedPercent * 100.0) / 100.0);
        return metrics;
    }
}
