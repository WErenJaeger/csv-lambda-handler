package com.myproject;

import org.apache.poi.ss.usermodel.*;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ExcelParser {

    public List<String[]> parse(InputStream inputStream) throws Exception {

        List<String[]> rows = new ArrayList<>();

        Workbook workbook = WorkbookFactory.create(inputStream);
        Sheet sheet = workbook.getSheetAt(0);

        // İlk satır başlık, atla
        boolean ilkSatir = true;

        for (Row row : sheet) {
            if (ilkSatir) { ilkSatir = false; continue; }

            List<String> cells = new ArrayList<>();
            for (Cell cell : row) {
                cells.add(cell.toString());
            }
            rows.add(cells.toArray(new String[0]));
        }

        workbook.close();
        return rows;
    }
}