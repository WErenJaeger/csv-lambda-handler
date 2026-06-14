package com.myproject;

import java.sql.*;
import java.util.*;

public class DatabaseService {

    private Connection connect() throws Exception {
        String url      = System.getenv("DB_URL");
        String user     = System.getenv("DB_USER");
        String password = System.getenv("DB_PASSWORD");

        Properties props = new Properties();
        props.setProperty("user", user);
        props.setProperty("password", password);
        props.setProperty("ssl", "true");
        props.setProperty("sslmode", "require");

        return DriverManager.getConnection(url, props);
    }

    // Upload log kaydı oluştur
    public int createUploadLog(String fileName, String fileType) throws Exception {
        Connection conn = connect();
        String sql = "INSERT INTO upload_log (file_name, file_type, status) VALUES (?, ?, 'PROCESSING') RETURNING id";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, fileName);
        stmt.setString(2, fileType);
        ResultSet rs = stmt.executeQuery();
        rs.next();
        int id = rs.getInt("id");
        conn.close();
        return id;
    }

    // Upload log güncelle
    public void updateUploadLog(int uploadId, int rowCount, int colCount, String status, String error, long durationMs) throws Exception {
        Connection conn = connect();
        String sql = "UPDATE upload_log SET row_count=?, column_count=?, status=?, error_message=?, duration_ms=? WHERE id=?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, rowCount);
        stmt.setInt(2, colCount);
        stmt.setString(3, status);
        stmt.setString(4, error);
        stmt.setLong(5, durationMs);
        stmt.setInt(6, uploadId);
        stmt.executeUpdate();
        conn.close();
    }

    // Ham veriyi kaydet
    public void saveRawData(int uploadId, List<Map<String, String>> rows) throws Exception {
        Connection conn = connect();
        String sql = "INSERT INTO raw_data (upload_id, row_index, data) VALUES (?, ?, ?::jsonb)";
        PreparedStatement stmt = conn.prepareStatement(sql);

        for (int i = 0; i < rows.size(); i++) {
            stmt.setInt(1, uploadId);
            stmt.setInt(2, i);
            stmt.setString(3, mapToJson(rows.get(i)));
            stmt.addBatch();
        }
        stmt.executeBatch();
        conn.close();
    }

    // Temizlenmiş veriyi kaydet
    public void saveCleanedData(int uploadId, List<Map<String, String>> rows) throws Exception {
        Connection conn = connect();
        String sql = "INSERT INTO cleaned_data (upload_id, row_index, data) VALUES (?, ?, ?::jsonb)";
        PreparedStatement stmt = conn.prepareStatement(sql);

        for (int i = 0; i < rows.size(); i++) {
            stmt.setInt(1, uploadId);
            stmt.setInt(2, i);
            stmt.setString(3, mapToJson(rows.get(i)));
            stmt.addBatch();
        }
        stmt.executeBatch();
        conn.close();
    }

    // İstatistikleri kaydet
    public void saveStats(int uploadId, List<Map<String, Object>> stats) throws Exception {
        Connection conn = connect();
        String sql = "INSERT INTO data_stats (upload_id, column_name, mean, median, std_dev, min_val, max_val, null_count, outlier_count) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement stmt = conn.prepareStatement(sql);

        for (Map<String, Object> stat : stats) {
            stmt.setInt(1, uploadId);
            stmt.setString(2, (String) stat.get("column_name"));
            stmt.setDouble(3, (Double) stat.getOrDefault("mean", 0.0));
            stmt.setDouble(4, (Double) stat.getOrDefault("median", 0.0));
            stmt.setDouble(5, (Double) stat.getOrDefault("std_dev", 0.0));
            stmt.setDouble(6, (Double) stat.getOrDefault("min_val", 0.0));
            stmt.setDouble(7, (Double) stat.getOrDefault("max_val", 0.0));
            stmt.setInt(8, (Integer) stat.getOrDefault("null_count", 0));
            stmt.setInt(9, (Integer) stat.getOrDefault("outlier_count", 0));
            stmt.addBatch();
        }
        stmt.executeBatch();
        conn.close();
    }

    private String mapToJson(Map<String, String> map) {
        StringBuilder sb = new StringBuilder("{");
        for (Map.Entry<String, String> entry : map.entrySet()) {
            sb.append("\"").append(entry.getKey()).append("\":");
            sb.append("\"").append(entry.getValue() != null ? entry.getValue().replace("\"", "\\\"") : "").append("\",");
        }
        if (sb.length() > 1) sb.setLength(sb.length() - 1);
        sb.append("}");
        return sb.toString();
    }
}