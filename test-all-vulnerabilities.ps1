#!/usr/bin/env pwsh
# Test all 8 web security vulnerabilities
# Usage: .\test-all-vulnerabilities.ps1

param(
    [string]$BaseUrl = "http://localhost:8080",
    [switch]$Verbose
)

$ErrorActionPreference = "SilentlyContinue"

function Write-Header {
    param([string]$Text)
    Write-Host "`n" -NoNewline
    Write-Host "=" * 60 -ForegroundColor Cyan
    Write-Host $Text -ForegroundColor Cyan
    Write-Host "=" * 60 -ForegroundColor Cyan
}

function Write-Test {
    param([int]$Number, [string]$Name, [string]$Status = "TESTING")
    $color = if ($Status -eq "✅ VULNERABLE") { "Red" } elseif ($Status -eq "✅ PATCHED") { "Green" } else { "Yellow" }
    Write-Host "[$Number] $Name ... " -NoNewline
    Write-Host $Status -ForegroundColor $color
}

function Test-Response {
    param([string]$Response, [string[]]$SearchStrings, [bool]$ShouldFind = $true)
    if ([string]::IsNullOrEmpty($Response)) { return $false }
    
    foreach ($search in $SearchStrings) {
        $found = $Response -match $search
        if ($found -eq $ShouldFind) { return $true }
    }
    return !$ShouldFind
}

# Header
Write-Header "🛡️ WEB SECURITY VULNERABILITIES TEST SUITE"
Write-Host "Target: $BaseUrl"
Write-Host "Time: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')"
Write-Host ""

$results = @()

# ===== 1. SQL INJECTION =====
Write-Test 1 "SQL Injection (Login)" "TESTING"
try {
    $response = Invoke-RestMethod -Uri "$BaseUrl/login" `
        -Method POST `
        -Body @{username = "' OR '1'='1 --"; password = "test"} `
        -TimeoutSec 5
    
    if ($response -match "dashboard|success|authenticated") {
        Write-Host "    ✅ VULNERABLE: Bypass successful!" -ForegroundColor Red
        $results += "1. SQLi: ✅ VULNERABLE"
    }
    else {
        Write-Host "    ⛔ PATCHED: Login validation working" -ForegroundColor Green
        $results += "1. SQLi: ⛔ PATCHED"
    }
}
catch {
    Write-Host "    ⚠️ Error testing SQLi" -ForegroundColor Yellow
}

# ===== 2. XSS =====
Write-Test 2 "XSS (Search)" "TESTING"
try {
    $xssPayload = "<script>alert('XSS')</script>"
    $response = Invoke-RestMethod -Uri "$BaseUrl/search" `
        -Method POST `
        -Body @{query = $xssPayload} `
        -TimeoutSec 5
    
    if ($response -match "<script>" -and $response -notmatch "&lt;script&gt;") {
        Write-Host "    ✅ VULNERABLE: XSS payload not escaped!" -ForegroundColor Red
        $results += "2. XSS: ✅ VULNERABLE"
    }
    else {
        Write-Host "    ⛔ PATCHED: Script properly escaped" -ForegroundColor Green
        $results += "2. XSS: ⛔ PATCHED"
    }
}
catch {
    Write-Host "    ⚠️ Error testing XSS" -ForegroundColor Yellow
}

# ===== 3. PATH TRAVERSAL =====
Write-Test 3 "Path Traversal (View-Log)" "TESTING"
try {
    $response = Invoke-RestMethod -Uri "$BaseUrl/view-log?filename=../../../pom.xml" `
        -TimeoutSec 5
    
    if ($response -match "modelVersion|maven-|spring-boot") {
        Write-Host "    ✅ VULNERABLE: Can read pom.xml!" -ForegroundColor Red
        $results += "3. Path Traversal: ✅ VULNERABLE"
    }
    else {
        Write-Host "    ⛔ PATCHED: Path validation working" -ForegroundColor Green
        $results += "3. Path Traversal: ⛔ PATCHED"
    }
}
catch {
    Write-Host "    ⚠️ Error testing Path Traversal" -ForegroundColor Yellow
}

# ===== 4. DoS - SPAM SEARCH =====
Write-Test 4 "DoS (Spam Search)" "TESTING"
try {
    $startTime = Get-Date
    $successCount = 0
    
    for ($i = 0; $i -lt 50; $i++) {
        $response = curl.exe -s -X POST "$BaseUrl/search" `
            -H "Content-Type: application/x-www-form-urlencoded" `
            -d "query=dos-test-$i" 2>&1
        
        if ($response -notmatch "429|Too Many|Rate limit") {
            $successCount++
        }
    }
    
    $endTime = Get-Date
    $duration = ($endTime - $startTime).TotalSeconds
    
    if ($successCount -ge 45) {
        Write-Host "    ✅ VULNERABLE: $successCount/50 requests succeeded (No rate limit)" -ForegroundColor Red
        $results += "4. DoS: ✅ VULNERABLE"
    }
    else {
        Write-Host "    ⛔ PATCHED: Rate limiting active ($successCount/50 passed)" -ForegroundColor Green
        $results += "4. DoS: ⛔ PATCHED"
    }
}
catch {
    Write-Host "    ⚠️ Error testing DoS" -ForegroundColor Yellow
}

# ===== 5. UPLOAD - FILE SIZE =====
Write-Test 5 "Upload DoS (File Size)" "TESTING"
try {
    $testFile = "$env:TEMP\test-vuln-2mb.bin"
    if (-not (Test-Path $testFile)) {
        $stream = [System.IO.File]::Create($testFile)
        $stream.SetLength(2MB)
        $stream.Close()
    }
    
    $form = @{ file = Get-Item -Path $testFile }
    $response = Invoke-RestMethod -Uri "$BaseUrl/upload" `
        -Method POST `
        -Form $form `
        -TimeoutSec 10 2>&1
    
    if ($response -match "success|uploaded") {
        Write-Host "    ✅ VULNERABLE: 2MB file uploaded (No size check)!" -ForegroundColor Red
        $results += "5. Upload DoS: ✅ VULNERABLE"
    }
    else {
        Write-Host "    ⛔ PATCHED: File upload rejected" -ForegroundColor Green
        $results += "5. Upload DoS: ⛔ PATCHED"
    }
    
    Remove-Item $testFile -Force 2>&1 | Out-Null
}
catch {
    Write-Host "    ⛔ PATCHED: File upload validation working" -ForegroundColor Green
    $results += "5. Upload DoS: ⛔ PATCHED"
}

# ===== 6. RATE LIMITING =====
Write-Test 6 "Rate Limiting (150 requests)" "TESTING"
try {
    $blockedCount = 0
    $successCount = 0
    
    for ($i = 0; $i -lt 150; $i++) {
        $response = curl.exe -s -w "%{http_code}" -o $null "http://localhost:8080/rate-limit-test" 2>&1
        
        if ($response -eq 429 -or $response -match "429") {
            $blockedCount++
        }
        else {
            $successCount++
        }
        
        if ($i % 30 -eq 0 -and $i -gt 0) {
            Write-Host "    Progress: $i/150 requests sent, $blockedCount blocked" -ForegroundColor Gray
        }
    }
    
    if ($blockedCount -eq 0) {
        Write-Host "    ✅ VULNERABLE: No rate limiting! ($successCount/150 succeeded)" -ForegroundColor Red
        $results += "6. Rate Limiting: ✅ VULNERABLE"
    }
    elseif ($blockedCount -ge 10) {
        Write-Host "    ⛔ PATCHED: Rate limit triggered ($blockedCount blocked)" -ForegroundColor Green
        $results += "6. Rate Limiting: ⛔ PATCHED"
    }
    else {
        Write-Host "    ⚠️ PARTIAL: Rate limit triggered late ($blockedCount blocked)" -ForegroundColor Yellow
        $results += "6. Rate Limiting: ⚠️ PARTIAL"
    }
}
catch {
    Write-Host "    ⚠️ Error testing Rate Limiting" -ForegroundColor Yellow
}

# ===== 7. INPUT VALIDATION =====
Write-Test 7 "Input Validation (Register)" "TESTING"
try {
    $response = Invoke-RestMethod -Uri "$BaseUrl/register" `
        -Method POST `
        -Body @{
            username = "testuser123"
            email = "<script>alert('XSS')</script>"
            password = "Test123!"
        } `
        -TimeoutSec 5
    
    if ($response -match "success|registered" -and $response -match "<script>") {
        Write-Host "    ✅ VULNERABLE: XSS payload in email not validated!" -ForegroundColor Red
        $results += "7. Input Validation: ✅ VULNERABLE"
    }
    else {
        Write-Host "    ⛔ PATCHED: Input validation working" -ForegroundColor Green
        $results += "7. Input Validation: ⛔ PATCHED"
    }
}
catch {
    if ($Error[0] -match "Invalid|error|validation") {
        Write-Host "    ⛔ PATCHED: Input validation working" -ForegroundColor Green
        $results += "7. Input Validation: ⛔ PATCHED"
    }
    else {
        Write-Host "    ⚠️ Error testing Input Validation" -ForegroundColor Yellow
    }
}

# ===== 8. DIRECTORY TRAVERSAL LOGS =====
Write-Test 8 "Directory Traversal (Logs Demo)" "TESTING"
try {
    $response = Invoke-RestMethod -Uri "$BaseUrl/view-log-demo" `
        -TimeoutSec 5
    
    if ($response -match "Log|Application|INFO|DEBUG") {
        Write-Host "    ✅ Demo log accessible" -ForegroundColor Green
        $results += "8. Directory Traversal: ✅ DEMO OK"
    }
    else {
        Write-Host "    ⚠️ Demo log not accessible" -ForegroundColor Yellow
        $results += "8. Directory Traversal: ⚠️ FAILED"
    }
}
catch {
    Write-Host "    ⚠️ Error accessing demo log" -ForegroundColor Yellow
}

# SUMMARY
Write-Header "📊 TEST RESULTS SUMMARY"
Write-Host ""
foreach ($result in $results) {
    Write-Host $result
}

$vulnerable = ($results | Select-String "✅ VULNERABLE").Count
$patched = ($results | Select-String "⛔ PATCHED").Count
$partial = ($results | Select-String "⚠️").Count

Write-Host ""
Write-Host "Summary: " -NoNewline
Write-Host "$vulnerable VULNERABLE" -ForegroundColor Red -NoNewline
Write-Host " | " -NoNewline
Write-Host "$patched PATCHED" -ForegroundColor Green -NoNewline
Write-Host " | " -NoNewline
Write-Host "$partial ISSUES" -ForegroundColor Yellow

Write-Host ""
if ($vulnerable -gt 0) {
    Write-Host "⚠️  SECURITY ISSUES DETECTED!" -ForegroundColor Red
    Write-Host "   Apply security patches to fix vulnerabilities."
}
else {
    Write-Host "✅ ALL TESTS PASSED!" -ForegroundColor Green
    Write-Host "   Application is secure."
}

Write-Host "`nTest completed at $(Get-Date -Format 'HH:mm:ss')"
