package com.myproject;

import java.util.*;

public class DataCleaner {

    public List<Map<String, String>> clean(List<Map<String, String>> rows) {
        List<Map<String, String>> cleaned = new ArrayList<>();

        for (Map<String, String> row : rows) {
            if (isEmptyRow(row)) continue;

            Map<String, String> cleanedRow = new HashMap<>();
            for (Map.Entry<String, String> entry : row.entrySet()) {
                String value = entry.getValue();
                if (value != null) {
                    value = value.trim();
                    value = normalizeDate(value);
                }
                cleanedRow.put(entry.getKey(), value);
            }
            cleaned.add(cleanedRow);
        }
        return cleaned;
    }

    private boolean isEmptyRow(Map<String, String> row) {
        return row.values().stream().allMatch(v -> v == null || v.trim().isEmpty());
    }

    private String normalizeDate(String value) {
        if (value.matches("\\d{2}/\\d{2}/\\d{4}")) {
            String[] parts = value.split("/");
            return parts[2] + "-" + parts[1] + "-" + parts[0];
        }
        return value;
    }
}