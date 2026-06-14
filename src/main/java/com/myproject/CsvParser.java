package com.myproject;

import com.opencsv.CSVReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public class CsvParser {

    public List<Map<String, String>> parse(InputStream inputStream) throws Exception {
        List<Map<String, String>> rows = new ArrayList<>();
        CSVReader reader = new CSVReader(new InputStreamReader(inputStream));

        String[] headers = reader.readNext();
        if (headers == null) return rows;

        String[] line;
        while ((line = reader.readNext()) != null) {
            Map<String, String> row = new LinkedHashMap<>();
            for (int i = 0; i < headers.length; i++) {
                row.put(headers[i].trim(), i < line.length ? line[i] : "");
            }
            rows.add(row);
        }
        reader.close();
        return rows;
    }
}