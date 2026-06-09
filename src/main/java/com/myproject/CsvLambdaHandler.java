package com.myproject;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;

import java.io.InputStream;
import java.util.List;

public class CsvLambdaHandler implements RequestHandler<S3Event, String> {

    private final AmazonS3 s3Client = AmazonS3ClientBuilder.defaultClient();
    private final CsvParser csvParser = new CsvParser();
    private final ExcelParser excelParser = new ExcelParser();
    private final DatabaseService dbService = new DatabaseService();

    @Override
    public String handleRequest(S3Event event, Context context) {

        String bucketName = event.getRecords().get(0).getS3().getBucket().getName();
        String fileName   = event.getRecords().get(0).getS3().getObject().getKey();

        context.getLogger().log("Dosya geldi: " + bucketName + "/" + fileName);

        try {
            S3Object s3Object = s3Client.getObject(bucketName, fileName);
            InputStream inputStream = s3Object.getObjectContent();

            List<String[]> rows;

            if (fileName.endsWith(".csv")) {
                context.getLogger().log("CSV işleniyor...");
                rows = csvParser.parse(inputStream);
            } else if (fileName.endsWith(".xlsx")) {
                context.getLogger().log("Excel işleniyor...");
                rows = excelParser.parse(inputStream);
            } else {
                return "Hata: Desteklenmeyen dosya türü";
            }

            context.getLogger().log("Toplam satır: " + rows.size());
            dbService.saveRows(rows);
            context.getLogger().log("Veritabanına kaydedildi!");

            return "Başarılı: " + rows.size() + " satır kaydedildi";

        } catch (Exception e) {
            context.getLogger().log("Hata: " + e.getMessage());
            return "Hata: " + e.getMessage();
        }
    }
}