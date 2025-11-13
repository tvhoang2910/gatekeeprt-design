# 🛡️ Gatekeeper Pattern - Demo Bảo Mật Web

Ứng dụng Spring Boot demo 8 lỗ hổng bảo mật web phổ biến và cách khắc phục bằng mẫu thiết kế Gatekeeper.

## 🚀 Chạy Ứng Dụng

```bash
mvn spring-boot:run
```

Truy cập: http://localhost:8080

## 🧪 Test Lỗ Hổng Bảo Mật

### 1. SQL Injection (Đăng Nhập)
- **URL**: http://localhost:8080/login
- **Test**: Username: `' OR '1'='1 --`, Password: `abc`
- **Kết quả**: Bypass đăng nhập thành công

### 2. XSS (Tìm Kiếm)
- **URL**: http://localhost:8080/search
- **Test**: Nhập `<script>alert('XSS')</script>`
- **Kết quả**: Script thực thi nếu chưa bật bảo mật

### 3. Path Traversal (Xem Log)
- **URL**: http://localhost:8080/view-log?filename=../../../pom.xml
- **Kết quả**: Đọc được file pom.xml

### 4. DoS (Spam Request)
- **URL**: http://localhost:8080/search
- **Test**: Gửi nhiều request liên tục
- **Kết quả**: Server chậm nếu chưa bật rate limiting

### 5. Upload DoS
- **URL**: http://localhost:8080/upload
- **Test**: Upload file lớn (>1MB)
- **Kết quả**: Server có thể bị ảnh hưởng

### 6. Rate Limiting Bypass
- **URL**: http://localhost:8080/rate-limit-test
- **Test**: Gửi >100 request/phút
- **Kết quả**: HTTP 429 nếu bật rate limiting

### 7. Input Validation
- **URL**: http://localhost:8080/register
- **Test**: Email: `<script>alert(1)</script>`
- **Kết quả**: Script lưu vào DB nếu chưa validate

### 8. Dashboard Giám Sát
- **URL**: http://localhost:8080/monitoring-dashboard
- **Xem**: Metrics, bot detection, performance

## 🔧 Bật Bảo Mật (Gatekeeper)

Để enable bảo mật, uncomment `// @Component` trong các file:

1. **RateLimitingFilter.java** - Giới hạn request
2. **BotDetectionFilter.java** - Phát hiện bot
3. **BenchmarkFilter.java** - Đo performance
4. **SecurityGateway.java** - Central security service

Sau đó restart ứng dụng.

## 🎯 Cách Tấn Công Rate Limit & Bot Detection

### Tấn Công Rate Limiting:
```bash
# Spam request để test rate limit
for($i=0; $i -lt 150; $i++) { curl.exe http://localhost:8080/rate-limit-test }
```

### Tấn Công Bot Detection:
```bash
# Test với User-Agent bot
curl -H "User-Agent: Googlebot/2.1" http://localhost:8080/
curl -H "User-Agent: python-requests/2.25.1" http://localhost:8080/
curl -H "User-Agent: curl/7.68.0" http://localhost:8080/
```

### PowerShell (Windows):
```powershell
# Test bot detection
Invoke-WebRequest -Uri http://localhost:8080/ -Headers @{ 'User-Agent' = 'Googlebot/2.1' }
Invoke-WebRequest -Uri http://localhost:8080/ -Headers @{ 'User-Agent' = 'python-requests/2.25.1' }
```

## 📊 API Endpoints

- `GET /api/metrics` - Lấy metrics JSON
- `GET /api/bot-count` - Số bot detected
- `POST /api/reset-bot-count` - Reset bot counter
- `POST /api/reset-metrics` - Reset all metrics

## ⚠️ Lưu Ý

- **Chỉ dùng cho học tập** trên localhost
- **Đừng deploy** lên production
- **Có thể chứa** lỗ hổng bảo mật thực

## 🏗️ Kiến Trúc

```
Client Request
    ↓
[RateLimitingFilter] (Order 1) - Giới hạn request
    ↓
[BotDetectionFilter] (Order 2) - Phát hiện bot
    ↓
[SecurityGateway] (Business Logic) - Validate/Sanitize
    ↓
Response
```