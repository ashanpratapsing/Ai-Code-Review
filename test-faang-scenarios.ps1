# Antigravity FAANG Dynamic Analysis E2E Validator
$ErrorActionPreference = "Stop"

Write-Host "==================================================" -ForegroundColor Cyan
Write-Host "   FAANG Dynamic Code Intelligence Validator" -ForegroundColor Cyan
Write-Host "==================================================" -ForegroundColor Cyan

$baseUrl = "http://localhost:8080"
$guidSuffix = [guid]::NewGuid().ToString().Substring(0,8)
$email = "validator-" + $guidSuffix + "@example.com"
$password = "valPass123"

# Helper function to invoke API
function Call-API($path, $method, $body = $null, $token = $null) {
    $headers = @{ "Content-Type" = "application/json" }
    if ($token) {
        $headers.Add("Authorization", "Bearer $token")
    }
    
    $params = @{
        Uri = "$baseUrl$path"
        Method = $method
        Headers = $headers
        UseBasicParsing = $true
    }
    if ($body) {
        $params.Add("Body", ($body | ConvertTo-Json -Depth 10))
    } elseif ($method -eq "POST" -or $method -eq "PUT") {
        $params.Add("Body", "{}")
    }
    
    return Invoke-RestMethod @params
}

# 1. Auth Setup
Write-Host ("Setting up validator account for " + $email + "...")
$signupRes = Call-API "/auth/signup" "POST" @{ name = "Validator"; email = $email; password = $password }

$headers = @{ "Content-Type" = "application/json" }
$loginRes = Invoke-WebRequest -Uri "$baseUrl/auth/login" -Method "POST" -Headers $headers -Body (@{ email = $email; password = $password } | ConvertTo-Json) -UseBasicParsing

# Extract token from Set-Cookie header
$token = $null
$cookies = $loginRes.Headers["Set-Cookie"]
if ($cookies -is [system.array]) {
    foreach ($cookie in $cookies) {
        if ($cookie -match 'access_token=([^;]+)') {
            $token = $Matches[1]
        }
    }
} else {
    if ($cookies -match 'access_token=([^;]+)') {
        $token = $Matches[1]
    }
}

if (-not $token) {
    Write-Host "Failed to extract access_token from cookie headers." -ForegroundColor Red
    exit 1
}
Write-Host "Logged in successfully!" -ForegroundColor Green

# 2. Project Setup
Write-Host "Creating validation project..."
$project = Call-API "/projects" "POST" @{ projectName = "E2E Dynamic Verification" } $token
$projectId = $project.id
Write-Host ("Project created ID: " + $projectId) -ForegroundColor Green

# Helper function to run full review cycle
function Run-Review-Cycle($filename, $code, $execCtx = $null) {
    # Upload Code
    $upload = Call-API "/code/upload" "POST" @{
        name = $filename
        content = $code
        language = "JAVA"
        project = @{ id = $projectId }
    } $token
    $fileId = $upload.id
    
    # Trigger Analysis
    Write-Host ("Triggering analysis for " + $filename + " ID: " + $fileId + "...")
    Call-API "/analyze/${fileId}?model=AUTO" "POST" $execCtx $token | Out-Null
    
    # Poll for completion
    Write-Host "Polling for metrics completion..."
    for ($i = 0; $i -lt 30; $i++) {
        Start-Sleep -Seconds 2
        $metrics = Call-API "/analyze/$fileId" "GET" $null $token
        if ($metrics.status -eq "COMPLETED") {
            Write-Host "Analysis finished!" -ForegroundColor Green
            return $metrics
        } elseif ($metrics.status -eq "FAILED") {
            Write-Host "Analysis FAILED!" -ForegroundColor Red
            return $metrics
        }
    }
    Write-Host "Analysis TIMEOUT!" -ForegroundColor Red
    exit 1
}

# ==========================================
# TEST CASE 1 — TREE MAP
# ==========================================
Write-Host "----------------------------------------" -ForegroundColor Yellow
Write-Host "TEST CASE 1: TreeMap Java Code" -ForegroundColor Yellow
Write-Host "----------------------------------------" -ForegroundColor Yellow

$treeMapCode = @"
import java.util.TreeMap;
import java.util.Map;

public class TreeMapDemo {
    public static void main(String[] args) {
        TreeMap<Integer, String> map = new TreeMap<>();
        map.put(102, "Bob");
        map.put(101, "Alice");
        for (Map.Entry<Integer, String> entry : map.entrySet()) {
            System.out.println(entry.getKey() + "=" + entry.getValue());
        }
    }
}
"@

$metrics1 = Run-Review-Cycle "TreeMapDemo.java" $treeMapCode
$jsonStr = $metrics1 | ConvertTo-Json -Depth 10

# Assert TreeMap dynamic contents
$passed1 = $true
if ($jsonStr -notmatch 'Red-Black Tree' -and $jsonStr -notmatch 'TreeMap') {
    Write-Host "Missing Red-Black Tree explanation!" -ForegroundColor Red
    $passed1 = $false
}
if ($jsonStr -notmatch 'O\(log n\)' -and $jsonStr -notmatch 'O\(log N\)') {
    Write-Host "Missing O(log n) time complexity!" -ForegroundColor Red
    $passed1 = $false
}
if ($jsonStr -notmatch 'sorted') {
    Write-Host "Missing sorted order/iteration order!" -ForegroundColor Red
    $passed1 = $false
}
if ($jsonStr -match 'Use loggers instead of System.out' -or $jsonStr -match 'SLF4J') {
    Write-Host "Leak: Generic logger suggestion appeared!" -ForegroundColor Red
    $passed1 = $false
}

if ($passed1) {
    Write-Host "TEST CASE 1 PASSED!" -ForegroundColor Green
} else {
    Write-Host "TEST CASE 1 FAILED!" -ForegroundColor Red
    exit 1
}

# ==========================================
# TEST CASE 2 — BINARY SEARCH
# ==========================================
Write-Host "----------------------------------------" -ForegroundColor Yellow
Write-Host "TEST CASE 2: Binary Search Java Code" -ForegroundColor Yellow
Write-Host "----------------------------------------" -ForegroundColor Yellow

$binarySearchCode = @"
public class BinarySearch {
    public static int search(int[] arr, int target) {
        int low = 0, high = arr.length - 1;
        while (low <= high) {
            int mid = low + (high - low) / 2;
            if (arr[mid] == target) return mid;
            if (arr[mid] < target) low = mid + 1;
            else high = mid - 1;
        }
        return -1;
    }
}
"@

$metrics2 = Run-Review-Cycle "BinarySearch.java" $binarySearchCode
$jsonStr2 = $metrics2 | ConvertTo-Json -Depth 10

# Assert Binary Search dynamic contents
$passed2 = $true
if ($jsonStr2 -notmatch 'divide-and-conquer' -and $jsonStr2 -notmatch 'Divide and Conquer') {
    Write-Host "Missing divide-and-conquer explanation!" -ForegroundColor Red
    $passed2 = $false
}
if ($jsonStr2 -notmatch 'sorted') {
    Write-Host "Missing sorted array requirement!" -ForegroundColor Red
    $passed2 = $false
}
if ($jsonStr2 -notmatch 'O\(log n\)' -and $jsonStr2 -notmatch 'O\(log N\)') {
    Write-Host "Missing O(log n) complexity!" -ForegroundColor Red
    $passed2 = $false
}
if ($jsonStr2 -match 'TreeMap' -or $jsonStr2 -match 'Red-Black Tree') {
    Write-Host "Leak: TreeMap insights appeared!" -ForegroundColor Red
    $passed2 = $false
}

if ($passed2) {
    Write-Host "TEST CASE 2 PASSED!" -ForegroundColor Green
} else {
    Write-Host "TEST CASE 2 FAILED!" -ForegroundColor Red
    exit 1
}

# ==========================================
# TEST CASE 3 — MULTITHREADING
# ==========================================
Write-Host "----------------------------------------" -ForegroundColor Yellow
Write-Host "TEST CASE 3: Multithreading Java Code" -ForegroundColor Yellow
Write-Host "----------------------------------------" -ForegroundColor Yellow

$multithreadCode = @"
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadPoolDemo {
    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        for (int i = 0; i < 5; i++) {
            executor.submit(() -> {
                System.out.println(Thread.currentThread().getName() + " running task");
            });
        }
        executor.shutdown();
    }
}
"@

$metrics3 = Run-Review-Cycle "ThreadPoolDemo.java" $multithreadCode
$jsonStr3 = $metrics3 | ConvertTo-Json -Depth 10

# Assert Multithreading dynamic contents
$passed3 = $true
if (-not $metrics3.concurrencyAnalysis) {
    Write-Host "Missing concurrencyAnalysis field!" -ForegroundColor Red
    $passed3 = $false
}
if ($jsonStr3 -notmatch 'concurrency' -and $jsonStr3 -notmatch 'Concurrency') {
    Write-Host "Missing concurrency explanation!" -ForegroundColor Red
    $passed3 = $false
}
if ($jsonStr3 -notmatch 'synchronization' -and $jsonStr3 -notmatch 'synchronized') {
    Write-Host "Missing synchronization risks!" -ForegroundColor Red
    $passed3 = $false
}
if ($jsonStr3 -notmatch 'race condition') {
    Write-Host "Missing race conditions!" -ForegroundColor Red
    $passed3 = $false
}
if ($jsonStr3 -notmatch 'lifecycle' -and $jsonStr3 -notmatch 'terminate') {
    Write-Host "Missing thread lifecycle!" -ForegroundColor Red
    $passed3 = $false
}

if ($passed3) {
    Write-Host "TEST CASE 3 PASSED!" -ForegroundColor Green
} else {
    Write-Host "TEST CASE 3 FAILED!" -ForegroundColor Red
    exit 1
}

# ==========================================
# TEST CASE 4 — SCANNER INPUT
# ==========================================
Write-Host "----------------------------------------" -ForegroundColor Yellow
Write-Host "TEST CASE 4: Scanner Input Java Code" -ForegroundColor Yellow
Write-Host "----------------------------------------" -ForegroundColor Yellow

$scannerCode = @"
import java.util.Scanner;
public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int a = sc.nextInt();
        int b = sc.nextInt();
        System.out.println("Result: " + (a * b));
    }
}
"@

# Run in sandbox first to get results
Write-Host "Running code execution via Sandbox first..."
$executionRes = Call-API "/execute" "POST" @{
    code = $scannerCode
    language = "JAVA"
    testCases = @(
        @{ id = 1; input = "6 7"; expectedOutput = "Result: 42" }
    )
} $token

if ($executionRes.status -ne "SUCCESS") {
    Write-Host "Sandbox execution failed!" -ForegroundColor Red
    exit 1
}

$execCtx = @{
    executionStatus = $executionRes.status
    compileError = $executionRes.compileError
    executionResults = $executionRes.results
}

# Run code review with execution context
$metrics4 = Run-Review-Cycle "Main.java" $scannerCode $execCtx
$jsonStr4 = $metrics4 | ConvertTo-Json -Depth 10

# Assert Scanner dynamic contents
$passed4 = $true
if ($jsonStr4 -notmatch 'Scanner' -and $jsonStr4 -notmatch 'input') {
    Write-Host "Missing Scanner/input handling explanation!" -ForegroundColor Red
    $passed4 = $false
}
if ($metrics4.runtimeAnalysis -notmatch 'PASSED' -and $metrics4.runtimeAnalysis -notmatch 'Result: 42') {
    Write-Host "Missing sandbox execution output matching in runtimeAnalysis!" -ForegroundColor Red
    $passed4 = $false
}

if ($passed4) {
    Write-Host "TEST CASE 4 PASSED!" -ForegroundColor Green
} else {
    Write-Host "TEST CASE 4 FAILED!" -ForegroundColor Red
    exit 1
}

# ==========================================
# TEST CASE 5 — RUNTIME ERROR
# ==========================================
Write-Host "----------------------------------------" -ForegroundColor Yellow
Write-Host "TEST CASE 5: Runtime Error Java Code" -ForegroundColor Yellow
Write-Host "----------------------------------------" -ForegroundColor Yellow

$runtimeErrorCode = @"
public class Main {
    public static void main(String[] args) {
        int a = 10;
        int b = 0;
        System.out.println(a / b);
    }
}
"@

# Run in sandbox first to get error
Write-Host "Running code execution via Sandbox first..."
$executionResErr = Call-API "/execute" "POST" @{
    code = $runtimeErrorCode
    language = "JAVA"
    testCases = @(
        @{ id = 1; input = ""; expectedOutput = "" }
    )
} $token

$execCtxErr = @{
    executionStatus = $executionResErr.status
    compileError = $executionResErr.compileError
    executionResults = $executionResErr.results
}

# Run code review with execution context containing failure
$metrics5 = Run-Review-Cycle "Main.java" $runtimeErrorCode $execCtxErr
$jsonStr5 = $metrics5 | ConvertTo-Json -Depth 10

# Assert Runtime Error dynamic contents
$passed5 = $true
if ($jsonStr5 -notmatch 'ArithmeticException' -and $jsonStr5 -notmatch 'by zero' -and $jsonStr5 -notmatch 'failed') {
    Write-Host "Missing ArithmeticException/division by zero details!" -ForegroundColor Red
    $passed5 = $false
}
if ($jsonStr5 -notmatch 'line 5') {
    Write-Host "Missing failing line identification (line 5)!" -ForegroundColor Red
    $passed5 = $false
}

if ($passed5) {
    Write-Host "TEST CASE 5 PASSED!" -ForegroundColor Green
} else {
    Write-Host "TEST CASE 5 FAILED!" -ForegroundColor Red
    exit 1
}

Write-Host "==================================================" -ForegroundColor Green
Write-Host "   ALL 5 FAANG DYNAMIC ANALYSIS SCENARIOS PASSED!" -ForegroundColor Green
Write-Host "==================================================" -ForegroundColor Green
