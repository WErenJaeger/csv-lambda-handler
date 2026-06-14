package com.myproject;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;

import java.io.InputStream;
import java.util.*;

public class CsvLambdaHandler implements RequestHandler<S3Event, String> {

    private final AmazonS3 s3Client = AmazonS3ClientBuilder.defaultClient();
    private final CsvParser csvParser = new CsvParser();
    private final ExcelParser excelParser = new ExcelParser();
    private final DataCleaner dataCleaner = new DataCleaner();
    private final StatisticsService statsService = new StatisticsService();
    private final DatabaseService dbService = new DatabaseService();

    @Override
    public String handleRequest(S3Event event, Context context) {
        String bucketName = event.getRecords().get(0).getS3().getBucket().getName();
        String fileName   = event.getRecords().get(0).getS3().getObject().getKey();
        String fileType   = fileName.endsWith(".csv") ? "CSV" : "EXCEL";
        long startTime    = System.currentTimeMillis();
        int uploadId      = -1;

        try {
            context.getLogger().log("Dosya geldi: " + fileName);
            uploadId = dbService.createUploadLog(fileName, fileType);

            S3Object s3Object = s3Client.getObject(bucketName, fileName);
            InputStream inputStream = s3Object.getObjectContent();

            List<Map<String, String>> rows;
            if (fileName.endsWith(".csv")) {
                rows = csvParser.parse(inputStream);
            } else if (fileName.endsWith(".xlsx")) {
                rows = excelParser.parse(inputStream);
            } else {
                return "Hata: Desteklenmeyen dosya türü";
            }

            context.getLogger().log("Ham satır sayısı: " + rows.size());
            dbService.saveRawData(uploadId, rows);

            List<Map<String, String>> cleanedRows = dataCleaner.clean(rows);
            context.getLogger().log("Temizlenen satır sayısı: " + cleanedRows.size());
            dbService.saveCleanedData(uploadId, cleanedRows);

            List<Map<String, Object>> stats = statsService.analyze(cleanedRows);
            context.getLogger().log("İstatistik hesaplanan kolon sayısı: " + stats.size());
            dbService.saveStats(uploadId, stats);

            long duration = System.currentTimeMillis() - startTime;
            dbService.updateUploadLog(uploadId, rows.size(), rows.isEmpty() ? 0 : rows.get(0).size(), "SUCCESS", null, duration);

            return "Başarılı: " + rows.size() + " satır işlendi, " + stats.size() + " kolon analiz edildi.";

        } catch (Exception e) {
            context.getLogger().log("Hata: " + e.getMessage());
            try {
                if (uploadId != -1) {
                    dbService.updateUploadLog(uploadId, 0, 0, "ERROR", e.getMessage(), System.currentTimeMillis() - startTime);
                }
            } catch (Exception ex) {
                context.getLogger().log("Log güncellenemedi: " + ex.getMessage());
            }
            return "Hata: " + e.getMessage();
        }
    }
}