package com.myproject;

import org.apache.poi.ss.usermodel.*;
import java.io.InputStream;
import java.util.*;

public class ExcelParser {

    public List<Map<String, String>> parse(InputStream inputStream) throws Exception {
        List<Map<String, String>> rows = new ArrayList<>();

        Workbook workbook = WorkbookFactory.create(inputStream);
        Sheet sheet = workbook.getSheetAt(0);

        Iterator<Row> rowIterator = sheet.iterator();
        if (!rowIterator.hasNext()) return rows;

        Row headerRow = rowIterator.next();
        List<String> headers = new ArrayList<>();
        for (Cell cell : headerRow) {
            headers.add(cell.toString().trim());
        }

        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            Map<String, String> rowMap = new LinkedHashMap<>();
            for (int i = 0; i < headers.size(); i++) {
                Cell cell = row.getCell(i);
                rowMap.put(headers.get(i), cell != null ? cell.toString() : "");
            }
            rows.add(rowMap);
        }

        workbook.close();
        return rows;
    }
}