# Script để test bot attack và xem metrics real-time
param(
    [int]$Count = 10
)

Write-Host "=== BOT ATTACK TEST ===" -ForegroundColor Red
Write-Host "Gửi $Count bot requests..." -ForegroundColor Yellow

$botUserAgents = @(
    "curl/7.68.0",
    "python-requests/2.25.1", 
    "Googlebot/2.1",
    "wget/1.20.3",
    "Bingbot/2.0",
    "Scrapy/2.5.0 (bot; +http://www.scrapy.org)"
)

for ($i = 1; $i -le $Count; $i++) {
    $ua = $botUserAgents[(Get-Random -Minimum 0 -Maximum $botUserAgents.Length)]
    try {
        Invoke-WebRequest -Uri "http://localhost:8080/" -Headers @{"User-Agent"=$ua} -UseBasicParsing -ErrorAction SilentlyContinue | Out-Null
    } catch {
        # Bot bị chặn - expected
    }
    Write-Host "." -NoNewline -ForegroundColor Red
    Start-Sleep -Milliseconds 100
}

Write-Host ""
Write-Host ""
Write-Host "=== METRICS HIỆN TẠI ===" -ForegroundColor Green

$metrics = (Invoke-WebRequest -Uri "http://localhost:8080/api/metrics" -UseBasicParsing).Content | ConvertFrom-Json

Write-Host "Total Requests:     $($metrics.totalRequests)" -ForegroundColor Cyan
Write-Host "Bots Detected:      $($metrics.botsDetected)" -ForegroundColor Red
Write-Host "Failed Requests:    $($metrics.failedRequests)" -ForegroundColor Yellow
Write-Host "Success Rate:       $($metrics.successRate)%" -ForegroundColor Green
Write-Host "Avg Response Time:  $($metrics.averageResponseTime)ms" -ForegroundColor Magenta

Write-Host ""
Write-Host "🌐 Mở dashboard: http://localhost:8080/monitoring-dashboard" -ForegroundColor Yellow
Write-Host "   Dashboard sẽ tự động cập nhật metrics mỗi 5 giây!" -ForegroundColor Gray
