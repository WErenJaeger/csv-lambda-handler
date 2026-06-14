package com.myproject;

import java.util.*;
import java.util.stream.*;

public class StatisticsService {

    public List<Map<String, Object>> analyze(List<Map<String, String>> rows) {
        if (rows.isEmpty()) return new ArrayList<>();

        List<Map<String, Object>> stats = new ArrayList<>();
        Set<String> columns = rows.get(0).keySet();

        for (String col : columns) {
            List<Double> numbers = new ArrayList<>();
            int nullCount = 0;

            for (Map<String, String> row : rows) {
                String val = row.get(col);
                if (val == null || val.trim().isEmpty()) {
                    nullCount++;
                } else {
                    try {
                        numbers.add(Double.parseDouble(val.trim()));
                    } catch (NumberFormatException e) {
                        // sayısal değil, atla
                    }
                }
            }

            if (!numbers.isEmpty()) {
                Map<String, Object> stat = new HashMap<>();
                stat.put("column_name", col);
                stat.put("mean", mean(numbers));
                stat.put("median", median(numbers));
                stat.put("std_dev", stdDev(numbers));
                stat.put("min_val", Collections.min(numbers));
                stat.put("max_val", Collections.max(numbers));
                stat.put("null_count", nullCount);
                stat.put("outlier_count", countOutliers(numbers));
                stats.add(stat);
            }
        }
        return stats;
    }

    private double mean(List<Double> nums) {
        return nums.stream().mapToDouble(d -> d).average().orElse(0);
    }

    private double median(List<Double> nums) {
        List<Double> sorted = nums.stream().sorted().collect(Collectors.toList());
        int mid = sorted.size() / 2;
        return sorted.size() % 2 == 0 ? (sorted.get(mid - 1) + sorted.get(mid)) / 2 : sorted.get(mid);
    }

    private double stdDev(List<Double> nums) {
        double mean = mean(nums);
        return Math.sqrt(nums.stream().mapToDouble(d -> Math.pow(d - mean, 2)).average().orElse(0));
    }

    private int countOutliers(List<Double> nums) {
        List<Double> sorted = nums.stream().sorted().collect(Collectors.toList());
        int n = sorted.size();
        double q1 = sorted.get(n / 4);
        double q3 = sorted.get(3 * n / 4);
        double iqr = q3 - q1;
        double lower = q1 - 1.5 * iqr;
        double upper = q3 + 1.5 * iqr;
        return (int) nums.stream().filter(d -> d < lower || d > upper).count();
    }
}