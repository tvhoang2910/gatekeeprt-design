
```bash
# Spam request để test rate limit
for($i=0; $i -lt 150; $i++) { curl.exe -H "User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36" http://localhost:8080/rate-limit-test }
```

# bot
```bash
.\bot-attack.ps1 -Count 10
```