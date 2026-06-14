# CSV/Excel Lambda Handler

Serverless bir veri işleme pipeline'ı. Kullanıcı AWS S3'e CSV veya Excel dosyası yüklediğinde AWS Lambda otomatik olarak tetiklenir, dosyayı işler ve Supabase (PostgreSQL) veritabanına kaydeder.

## Mimari

Kullanıcı → CSV/Excel yükler → AWS S3 → AWS Lambda tetiklenir → Dosya işlenir → Supabase (PostgreSQL)

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
- Verileri PostgreSQL'e batch insert yapar
- AWS CloudWatch ile loglama
- Supabase Session Pooler ile IPv4 uyumlu bağlantı

## Kurulum

### 1. Gereksinimler
- Java 21
- Maven
- AWS CLI
- Supabase hesabı

### 2. Supabase'de tablo oluştur

```sql
CREATE TABLE veriler (
    id         SERIAL PRIMARY KEY,
    kolon1     TEXT,
    kolon2     TEXT,
    kolon3     TEXT,
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
5. Veriler PostgreSQL'e kaydedilir
6. Sonuç CloudWatch'a loglanır