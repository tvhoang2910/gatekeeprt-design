# 🔧 Fix Dashboard Real-Time Update

## Vấn đề đã sửa

Dashboard **không tự động cập nhật** metrics khi có bot attack. Phải reload trang mới thấy số liệu mới.

### Nguyên nhân
JavaScript cố gắng update các HTML elements không tồn tại (`requestsProgress`, `botProgress`, etc.), gây lỗi và dừng execution của auto-refresh.

### Giải pháp
1. ✅ Thêm null-check cho tất cả DOM elements trước khi update
2. ✅ Thêm console.log để debug 
3. ✅ Cải thiện error handling trong toggleAutoRefresh()
4. ✅ Đảm bảo @Order(1) cho BenchmarkFilter để đếm tất cả requests

## Cách test Dashboard Real-Time

### Bước 1: Khởi động ứng dụng
```powershell
.\mvnw.cmd spring-boot:run
```

### Bước 2: Mở Dashboard
1. Mở browser: **http://localhost:8080/monitoring-dashboard**
2. Nhấn **F12** để mở Developer Console
3. Chuyển sang tab **Console**

### Bước 3: Quan sát Auto-Refresh
Trong Console, bạn sẽ thấy logs mỗi 5 giây:
```
🚀 Initializing dashboard...
🔄 Fetching metrics from /api/metrics...
📊 Received metrics: {totalRequests: 0, botsDetected: 0, ...}
✅ Metrics updated: {total: 0, botCount: 0, ...}
✅ Dashboard initialized with auto-refresh
🔄 Auto-refresh toggled: true
✅ Auto-refresh enabled (5 seconds interval)
```

### Bước 4: Chạy Bot Attack
**Terminal mới**, chạy:
```powershell
.\test-realtime.ps1
```

Hoặc manual:
```powershell
# Gửi bot requests
Invoke-WebRequest -Uri "http://localhost:8080/" -Headers @{"User-Agent"="curl/7.68.0"} -UseBasicParsing

# Hoặc dùng script
.\bot-attack.ps1 -Count 50
```

### Bước 5: Kiểm tra Dashboard
**KHÔNG CẦN RELOAD TRANG**, bạn sẽ thấy:

✅ **Metrics tự động cập nhật:**
- Bot Đã Phát Hiện: tăng real-time
- Tổng Số Request: tăng real-time  
- Thời Gian Phản Hồi: cập nhật real-time
- Last updated: hiển thị thời gian hiện tại

✅ **Console logs:**
```
🔄 Fetching metrics from /api/metrics...
📊 Received metrics: {totalRequests: 50, botsDetected: 30, ...}
✅ Metrics updated: {total: 50, botCount: 30, ...}
```

## Troubleshooting

### Dashboard không cập nhật?

1. **Kiểm tra Console có lỗi đỏ không**
   - Nếu có lỗi → báo lại để debug

2. **Kiểm tra checkbox "Enable auto-refresh"**
   - Phải được **tick** (mặc định là tick)
   - Nếu bỏ tick → metrics không auto-update

3. **Hard Refresh**
   - Windows: `Ctrl + Shift + R` hoặc `Ctrl + F5`
   - Mac: `Cmd + Shift + R`
   - Hoặc: Clear cache và reload

4. **Kiểm tra API metrics**
   ```powershell
   Invoke-WebRequest -Uri "http://localhost:8080/api/metrics" -UseBasicParsing | Select-Object -ExpandProperty Content
   ```
   Phải trả về JSON với `botsDetected`, `totalRequests`, etc.

### Console không có logs?

1. **Refresh trang** (hard refresh)
2. **Kiểm tra tab Console** (không phải Network hay Elements)
3. **Filter logs**: Không được filter ẩn logs

## API Endpoints

| Endpoint | Method | Mô tả |
|----------|--------|-------|
| `/api/metrics` | GET | Lấy tất cả metrics (JSON) |
| `/api/metrics/text` | GET | Lấy metrics dạng text |
| `/api/reset-bot-count` | POST | Reset bot count |
| `/api/reset-metrics` | POST | Reset tất cả metrics |

## Scripts

| Script | Mô tả |
|--------|-------|
| `bot-attack.ps1` | Gửi bot requests để test |
| `test-realtime.ps1` | Test dashboard real-time với hướng dẫn đầy đủ |

## Technical Details

### Filter Order
```java
@Component
@Order(1)
public class BenchmarkFilter implements Filter {
    // Chạy TRƯỚC để đếm tất cả requests
}

@Component  
@Order(2)
public class BotDetectionFilter implements Filter {
    // Chạy SAU để phát hiện bot
}
```

### Auto-Refresh Mechanism
```javascript
// Init khi load trang
window.addEventListener('DOMContentLoaded', initDashboard);

// Auto-refresh mỗi 5 giây
autoRefreshInterval = setInterval(updateMetrics, 5000);

// Fetch từ API
fetch('/api/metrics')
  .then(response => response.json())
  .then(data => updateUI(data));
```

### Safe DOM Updates
```javascript
// Kiểm tra element tồn tại trước khi update
const botCountEl = document.getElementById('botCount');
if (botCountEl) botCountEl.textContent = botCount;
```

## Kết quả

✅ Dashboard cập nhật real-time mỗi 5 giây
✅ Bot count tăng ngay khi có bot attack
✅ Không cần reload trang
✅ Console logs rõ ràng để debug
✅ Error handling tốt hơn
