package com.myproject;
import java.util.Properties;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.List;

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

    public void saveRows(List<String[]> rows) throws Exception {

        Connection conn = connect();

        String sql = "INSERT INTO veriler (kolon1, kolon2, kolon3) VALUES (?, ?, ?)";
        PreparedStatement stmt = conn.prepareStatement(sql);

        for (String[] row : rows) {
            stmt.setString(1, row[0]);
            stmt.setString(2, row[1]);
            stmt.setString(3, row[2]);
            stmt.addBatch();
        }

        stmt.executeBatch();
        stmt.close();
        conn.close();
    }
}