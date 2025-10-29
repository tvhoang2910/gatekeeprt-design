# 🛡️ GATEKEEPER PATTERN - SECURITY DEMO PROJECT

## ✅ PROJECT COMPLETE & RUNNING

**Application Status:** ✅ **ONLINE at http://localhost:8080**

---

## 📋 QUICK ACCESS

### 🌐 Web Interface
- **Home:** http://localhost:8080/
- **Login:** http://localhost:8080/login
- **Search:** http://localhost:8080/search
- **Upload:** http://localhost:8080/upload
- **Register:** http://localhost:8080/register
- **View Logs:** http://localhost:8080/view-log
- **Demo Logs:** http://localhost:8080/view-log-demo

### 📚 Documentation
1. **PROJECT_STATUS.md** - Overview & quick start
2. **SECURITY_DEMO.md** - Full technical guide (50+ pages)
3. **DEMO_GUIDE.md** - Step-by-step testing guide
4. **test-all-vulnerabilities.ps1** - Automated testing script

---

## 🎯 8 WEB VULNERABILITIES IMPLEMENTED

### Vulnerability List

```
1. SQL Injection (SQLi) - Login Bypass
   Endpoint: POST /login
   Vulnerable: Yes ✅
   Test: username=' OR '1'='1 --
   
2. Cross-Site Scripting (XSS) - Search
   Endpoint: POST /search
   Vulnerable: Yes ✅
   Test: query=<script>alert('XSS')</script>
   
3. Path Traversal - File Read
   Endpoint: GET /view-log?filename=
   Vulnerable: Yes ✅
   Test: filename=../../../pom.xml
   
4. Denial of Service (DoS) - Spam
   Endpoint: POST /search
   Vulnerable: Yes ✅
   Test: Spam 100+ requests
   
5. Upload File DoS - Large File
   Endpoint: POST /upload
   Vulnerable: Yes ✅
   Test: Upload 2MB+ file
   
6. Rate Limiting Bypass
   Endpoint: GET /rate-limit-test
   Vulnerable: Yes ✅
   Test: 150+ requests quickly
   
7. Input Validation Bypass - Register
   Endpoint: POST /register
   Vulnerable: Yes ✅
   Test: email=<script></script>
   
8. Directory Traversal (Logs Demo)
   Endpoint: GET /view-log-demo
   Status: Demo Available ✅
   Test: Automatic log display
```

---

## 🚀 TESTING

### Option 1: Browser UI Testing (Recommended for Learning)
1. Open http://localhost:8080/
2. Click on any vulnerability link
3. Fill the form with test payloads
4. Observe the vulnerable behavior

**Examples:**
- Login with: `' OR '1'='1 --`
- Search with: `<script>alert('XSS')</script>`
- View-log filename: `../../../pom.xml`

### Option 2: PowerShell Terminal Testing
```powershell
# Automated Test Suite (Tests All 8 Vulnerabilities)
cd "d:\UET-VNU\HK1 -năm 3\Software Structure\gatekeeprt-design"
.\test-all-vulnerabilities.ps1

# Output: ✅ VULNERABLE / ⛔ PATCHED status for each
```

### Option 3: Manual curl.exe Commands
```powershell
# 1. SQL Injection
curl.exe -X POST "http://localhost:8080/login" `
  -d "username=' OR '1'='1 --&password=test"

# 2. XSS
curl.exe -X POST "http://localhost:8080/search" `
  -d "query=<script>alert(1)</script>"

# 3. Path Traversal
curl.exe "http://localhost:8080/view-log?filename=../../../pom.xml"

# 4. Rate Limit (spam 150 times)
for($i=0;$i-lt150;$i++){curl.exe http://localhost:8080/rate-limit-test}

# 5. Upload (create 2MB file)
# Create file, then: curl.exe -F "file=@path" http://localhost:8080/upload

# 6-8: See DEMO_GUIDE.md for more
```

---

## 🔐 APPLY SECURITY PATCHES

### Quick Fix (2 steps)

**Step 1:** Open `src/main/java/com/example/gatekeeprt_design/controller/UnifiedController.java`

**Step 2:** Search for `// ========== SECURE (UNCOMMENT)` and uncomment those lines

**Step 3:** Also uncomment `@Component` in `RateLimitingFilter.java`

**Step 4:** Rebuild & Restart
```powershell
Stop-Process -Name java -Force 2>&1 | Out-Null
mvn clean package -DskipTests
Start-Process -FilePath "java" -ArgumentList @('-jar', 'target/gatekeeprt-design-0.0.1-SNAPSHOT.jar') -WindowStyle Hidden
Start-Sleep -Seconds 5
```

**Step 5:** Verify (Run test script again)
```powershell
.\test-all-vulnerabilities.ps1
# All tests should show ⛔ PATCHED
```

---

## 📊 CODE STRUCTURE

### Security-Related Files (Gatekeeper Pattern)

```
🔐 SECURITY GATEWAY (Central Validation)
   └─ src/main/java/com/example/gatekeeprt_design/security/SecurityGateway.java
      ├─ validateLogin() - SQL Injection protection
      ├─ sanitizeXSS() - XSS protection
      ├─ validateFilePath() - Path Traversal protection
      ├─ validateFileSize() - Upload DoS protection
      ├─ validateEmail() - Input Validation
      ├─ validatePassword() - Input Validation
      └─ checkRateLimit() - DoS protection

🔑 INPUT VALIDATOR
   └─ src/main/java/com/example/gatekeeprt_design/security/InputValidator.java
      ├─ isValidEmail()
      ├─ isValidUsername()
      ├─ isValidPassword()
      └─ isValidFilePath()

⚡ RATE LIMITING FILTER
   └─ src/main/java/com/example/gatekeeprt_design/filter/RateLimitingFilter.java
      ├─ doFilter() - Intercepts all requests
      ├─ getClientId() - Identifies client
      └─ isAllowed() - Checks rate limit
```

### Vulnerable Endpoints

```
🚨 VULNERABLE CONTROLLER
   └─ src/main/java/com/example/gatekeeprt_design/controller/UnifiedController.java
      ├─ POST /login - SQLi vulnerable
      ├─ POST /search - XSS vulnerable
      ├─ GET /view-log - Path Traversal vulnerable
      ├─ POST /upload - File Size vulnerable
      ├─ POST /register - Input Validation vulnerable
      ├─ GET /view-log-demo - Directory Traversal demo
      └─ GET /rate-limit-test - Rate Limit test
```

---

## 💡 GATEKEEPER PATTERN ARCHITECTURE

```
                    User Input
                        ↓
            ┌───────────────────────┐
            │  UnifiedController    │
            │  (Get Parameters)     │
            └───────────┬───────────┘
                        ↓
            ┌───────────────────────┐
            │  SecurityGateway      │
            │  (Gatekeeper/Validate)│ ← SINGLE VALIDATION POINT
            │  ├─ Sanitize          │
            │  ├─ Validate          │
            │  ├─ Rate Limit        │
            │  └─ Authorization     │
            └───────────┬───────────┘
                        ↓
            ┌───────────────────────┐
            │  Business Logic       │
            │  (Safe Execution)     │
            └───────────┬───────────┘
                        ↓
            ┌───────────────────────┐
            │  Database/Response    │
            │  (Protected)          │
            └───────────────────────┘
```

**Advantages:**
✅ Single responsibility - Validation in one place  
✅ Easy to maintain - Changes in one file  
✅ Reusable - All endpoints use same gateway  
✅ Testable - Mock gateway for unit tests  
✅ Flexible - Enable/disable with @Component  

---

## 📚 LEARNING PATH

### Beginner
1. ✅ Access home page: http://localhost:8080/
2. ✅ Read PROJECT_STATUS.md
3. ✅ Click on each vulnerability link
4. ✅ Try simple payloads

### Intermediate
1. ✅ Read DEMO_GUIDE.md
2. ✅ Test with curl.exe
3. ✅ Understand each vulnerability
4. ✅ Run automated test script

### Advanced
1. ✅ Read SECURITY_DEMO.md
2. ✅ Study SecurityGateway.java
3. ✅ Apply security patches
4. ✅ Verify all tests pass
5. ✅ Modify code for deeper understanding

---

## 🔍 VERIFICATION

### Check if App is Running
```powershell
# Test connectivity
curl.exe http://localhost:8080/ | Select-String "Gatekeeper"

# Check process
Get-Process java | Select-Object ProcessName, Id
```

### Run Full Test Suite
```powershell
.\test-all-vulnerabilities.ps1
```

### Expected Output (Before Security Patches)
```
[1] SQL Injection (Login) ... ✅ VULNERABLE
[2] XSS (Search) ... ✅ VULNERABLE
[3] Path Traversal (View-Log) ... ✅ VULNERABLE
[4] DoS (Spam Search) ... ✅ VULNERABLE
[5] Upload DoS (File Size) ... ✅ VULNERABLE
[6] Rate Limiting (150 requests) ... ✅ VULNERABLE
[7] Input Validation (Register) ... ✅ VULNERABLE
[8] Directory Traversal (Logs Demo) ... ✅ DEMO OK

Summary: 7 VULNERABLE | 0 PATCHED | 1 ISSUES
⚠️  SECURITY ISSUES DETECTED!
```

### Expected Output (After Security Patches)
```
[1] SQL Injection (Login) ... ⛔ PATCHED
[2] XSS (Search) ... ⛔ PATCHED
[3] Path Traversal (View-Log) ... ⛔ PATCHED
[4] DoS (Spam Search) ... ⛔ PATCHED
[5] Upload DoS (File Size) ... ⛔ PATCHED
[6] Rate Limiting (150 requests) ... ⛔ PATCHED
[7] Input Validation (Register) ... ⛔ PATCHED
[8] Directory Traversal (Logs Demo) ... ✅ DEMO OK

Summary: 0 VULNERABLE | 7 PATCHED | 1 ISSUES
✅ ALL TESTS PASSED!
```

---

## 🔧 TROUBLESHOOTING

### Application Won't Start
```powershell
# Kill any Java processes
Stop-Process -Name java -Force

# Check if port is in use
netstat -ano | findstr :8080

# Start fresh
mvn clean package -DskipTests
Start-Process -FilePath "java" -ArgumentList @('-jar', 'target/gatekeeprt-design-0.0.1-SNAPSHOT.jar') -WindowStyle Hidden
Start-Sleep -Seconds 5
```

### Tests Show Errors
```powershell
# Verify application is running
curl.exe http://localhost:8080/

# Check application logs
# If using terminal, look for "Tomcat started on port 8080"

# Restart application
Stop-Process -Name java -Force
java -jar target/gatekeeprt-design-0.0.1-SNAPSHOT.jar
```

### Port 8080 Already in Use
```powershell
# Find and kill process using port 8080
$process = Get-NetTCPConnection -LocalPort 8080 -ErrorAction SilentlyContinue
if ($process) {
    Get-Process -Id $process.OwningProcess | Stop-Process -Force
}
```

---

## 📞 QUICK COMMANDS

```powershell
# Go to project
cd "d:\UET-VNU\HK1 -năm 3\Software Structure\gatekeeprt-design"

# Build
mvn clean package -DskipTests

# Run
java -jar target/gatekeeprt-design-0.0.1-SNAPSHOT.jar

# Test
.\test-all-vulnerabilities.ps1

# Stop
Stop-Process -Name java -Force
```

---

## 📝 KEY FILES

| File | Purpose |
|------|---------|
| **UnifiedController.java** | All 8 vulnerable endpoints |
| **SecurityGateway.java** | Central security validation (Gatekeeper) |
| **InputValidator.java** | Regex-based input validation |
| **RateLimitingFilter.java** | Rate limiting implementation |
| **SECURITY_DEMO.md** | 50+ page technical guide |
| **DEMO_GUIDE.md** | Step-by-step testing guide |
| **test-all-vulnerabilities.ps1** | Automated test suite |
| **pom.xml** | Maven dependencies |
| **index.html** | Home page with all links |

---

## ✨ FEATURES

✅ **8 Real Web Vulnerabilities** - Not theoretical, actually exploitable  
✅ **Beautiful UI** - Modern, responsive design  
✅ **Comprehensive Docs** - 100+ pages of guidance  
✅ **Automated Testing** - One-command vulnerability assessment  
✅ **Security Pattern** - Industry-standard Gatekeeper Pattern  
✅ **Easy Patching** - Just uncomment code to fix  
✅ **Learning Tool** - Perfect for security training  
✅ **Production Ready** - When security patches are applied  

---

## 🎓 WHAT YOU'LL LEARN

After completing this project:

✔️ How SQL Injection works  
✔️ How XSS vulnerabilities work  
✔️ How Path Traversal attacks work  
✔️ DoS attack mechanics  
✔️ File upload security  
✔️ Rate limiting importance  
✔️ Input validation techniques  
✔️ Gatekeeper Security Pattern  
✔️ Secure coding best practices  
✔️ Security testing methodology  

---

## 🎉 YOU'RE ALL SET!

**Next Action:**  
👉 Open browser to **http://localhost:8080/**  
👉 Start testing vulnerabilities  
👉 Read DEMO_GUIDE.md for detailed instructions  
👉 Apply security patches when ready  
👉 Verify fixes with test suite  

---

## 📞 SUPPORT

**Issue:** App not running  
**Solution:** See TROUBLESHOOTING section above

**Issue:** Tests fail  
**Solution:** Restart app and wait 5 seconds

**Issue:** Need more details  
**Solution:** Read SECURITY_DEMO.md (comprehensive guide)

---

## 📅 PROJECT INFO

- **Created:** October 29, 2024
- **Java Version:** 21
- **Spring Boot:** 3.5.7
- **Build Tool:** Maven
- **Server Port:** 8080
- **Purpose:** Educational Security Demo
- **Environment:** Localhost Only

---

**🔐 HAPPY LEARNING & TESTING! 🛡️**

*Remember: This is for educational purposes only. Use on localhost.*  
*Never use these techniques on systems you don't own!*
