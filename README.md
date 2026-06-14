# CSV/Excel Lambda Handler

Serverless ML Data Pipeline. AWS S3'e CSV veya Excel dosyası yüklendiğinde AWS Lambda otomatik tetiklenir, veriyi temizler, istatistiksel analiz yapar ve Supabase (PostgreSQL) veritabanına kaydeder.

## Mimari

CSV/Excel → AWS S3 → AWS Lambda → Veri Temizleme → İstatistik Analizi → Supabase (PostgreSQL)

## Teknolojiler

- **Java 21** — Lambda fonksiyonu
- **AWS Lambda** — Serverless compute
- **AWS S3** — Dosya depolama
- **Apache POI** — Excel (.xlsx) okuma
- **OpenCSV** — CSV okuma
- **Supabase (PostgreSQL)** — Veritabanı
- **Maven** — Dependency yönetimi

## Özellikler

- CSV ve Excel dosyalarını otomatik algılar
- S3'e dosya yüklendiğinde Lambda otomatik tetiklenir
- **Dinamik kolon desteği** — CSV başlıklarını otomatik okur
- **Veri temizleme** — Boş satır tespiti, whitespace temizleme, tarih normalizasyonu
- **Otomatik istatistik analizi** — Mean, median, std dev, min, max
- **Outlier tespiti** — IQR yöntemi ile anomali tespiti
- **Eksik veri raporlama** — Null count per kolon
- **Upload loglama** — Dosya adı, satır sayısı, işlem süresi, başarı/hata durumu
- AWS CloudWatch ile detaylı loglama

## Veritabanı Yapısı

| Tablo | Açıklama |
|---|---|
| `raw_data` | Ham yüklenen veri (JSONB) |
| `cleaned_data` | Temizlenmiş veri (JSONB) |
| `data_stats` | İstatistikler (mean, median, std_dev, outlier) |
| `upload_log` | Upload geçmişi ve işlem logları |

## Kurulum

### 1. Gereksinimler
- Java 21
- Maven
- AWS CLI
- Supabase hesabı

### 2. Supabase'de tabloları oluştur

```sql
CREATE TABLE raw_data (
    id SERIAL PRIMARY KEY, upload_id INTEGER,
    row_index INTEGER, data JSONB, created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE cleaned_data (
    id SERIAL PRIMARY KEY, upload_id INTEGER,
    row_index INTEGER, data JSONB, created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE data_stats (
    id SERIAL PRIMARY KEY, upload_id INTEGER,
    column_name TEXT, mean FLOAT, median FLOAT,
    std_dev FLOAT, min_val FLOAT, max_val FLOAT,
    null_count INTEGER, outlier_count INTEGER,
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE upload_log (
    id SERIAL PRIMARY KEY, file_name TEXT, file_type TEXT,
    row_count INTEGER, column_count INTEGER, status TEXT,
    error_message TEXT, duration_ms BIGINT,
    created_at TIMESTAMP DEFAULT NOW()
);
```

### 3. JAR oluştur

```bash
mvn clean package
```

### 4. AWS Lambda ayarları

| Ayar | Değer |
|---|---|
| Runtime | Java 21 |
| Handler | com.myproject.CsvLambdaHandler::handleRequest |
| Timeout | 1 dakika |
| Memory | 512 MB |

### 5. Environment Variables

| Key | Value |
|---|---|
| DB_URL | jdbc:postgresql://... |
| DB_USER | postgres.xxxx |
| DB_PASSWORD | şifre |

## Nasıl Çalışır?

1. Kullanıcı S3 bucket'a `.csv` veya `.xlsx` dosyası yükler
2. S3 eventi Lambda'yı tetikler
3. Lambda dosyayı S3'ten indirir
4. Dosya türüne göre CSV veya Excel parser çalışır
5. Ham veri `raw_data` tablosuna kaydedilir
6. `DataCleaner` boş satırları temizler, tarihleri normalize eder
7. Temizlenmiş veri `cleaned_data` tablosuna kaydedilir
8. `StatisticsService` sayısal kolonlar için istatistik hesaplar
9. İstatistikler `data_stats` tablosuna kaydedilir
10. Upload log `upload_log` tablosuna kaydedilir

## Örnek İstatistik Çıktısı

| Kolon | Mean | Median | Std Dev | Outlier |
|---|---|---|---|---|
| revenue | 2634.95 | 1599.92 | 2604.75 | 2 |
| quantity | 9.77 | 8.00 | 6.52 | 1 |
| price | 328.09 | 199.99 | 356.87 | 1 |