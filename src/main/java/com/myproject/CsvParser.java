package com.myproject;

import com.opencsv.CSVReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class CsvParser {

    public List<String[]> parse(InputStream inputStream) throws Exception {

        List<String[]> rows = new ArrayList<>();

        CSVReader reader = new CSVReader(new InputStreamReader(inputStream));
        String[] line;

        // İlk satır başlık, atla
        reader.readNext();

        while ((line = reader.readNext()) != null) {
            rows.add(line);
        }

        reader.close();
        return rows;
    }
}