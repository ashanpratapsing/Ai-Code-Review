# Sandbox Languages Dynamic Validator
$ErrorActionPreference = "Stop"

Write-Host "==================================================" -ForegroundColor Cyan
Write-Host "   Sandbox Languages Dynamic E2E Validator" -ForegroundColor Cyan
Write-Host "==================================================" -ForegroundColor Cyan

$baseUrl = "http://localhost:8080"
$guidSuffix = [guid]::NewGuid().ToString().Substring(0,8)
$email = "sandbox-val-" + $guidSuffix + "@example.com"
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
Write-Host ("Setting up validation account for " + $email + "...")
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

function Test-Language-Sandbox($lang, $code, $stdin, $expected) {
    Write-Host "----------------------------------------" -ForegroundColor Yellow
    Write-Host "Testing Language: $lang" -ForegroundColor Yellow
    Write-Host "----------------------------------------" -ForegroundColor Yellow
    
    $executionRes = Call-API "/execute" "POST" @{
        code = $code
        language = $lang
        testCases = @(
            @{ id = 1; input = $stdin; expectedOutput = $expected }
        )
    } $token

    if ($executionRes.status -ne "SUCCESS") {
        Write-Host "Execution FAILED! Status: $($executionRes.status)" -ForegroundColor Red
        if ($executionRes.compileError) {
            Write-Host "Compile Error: $($executionRes.compileError)" -ForegroundColor Red
        }
        return $false
    }
    
    $testResult = $executionRes.results[0]
    Write-Host "Execution Status: $($testResult.status)"
    Write-Host "Actual Output:   $($testResult.actualOutput)"
    Write-Host "Execution Time:  $($testResult.executionTimeMs) ms"
    
    if ($testResult.status -eq "PASSED") {
        Write-Host "PASSED!" -ForegroundColor Green
        return $true
    } else {
        Write-Host "FAILED! Error: $($testResult.error)" -ForegroundColor Red
        return $false
    }
}

# 1. Java
$javaCode = @"
import java.util.Scanner;
public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("Java execution: " + sc.nextLine());
    }
}
"@
$javaPassed = Test-Language-Sandbox "JAVA" $javaCode "Hello Java" "Java execution: Hello Java"

# 2. Python
$pythonCode = @"
import sys
input_data = sys.stdin.read().strip()
print(f"Python execution: {input_data}")
"@
$pythonPassed = Test-Language-Sandbox "PYTHON" $pythonCode "Hello Python" "Python execution: Hello Python"

# 3. JavaScript
$jsCode = @"
const fs = require('fs');
const input = fs.readFileSync(0, 'utf-8').trim();
console.log("JS execution: " + input);
"@
$jsPassed = Test-Language-Sandbox "JAVASCRIPT" $jsCode "Hello JS" "JS execution: Hello JS"

# 4. C
$cCode = @"
#include <stdio.h>
int main() {
    char name[100];
    if (scanf("%99s", name) == 1) {
        printf("C execution: Hello %s\n", name);
    }
    return 0;
}
"@
$cPassed = Test-Language-Sandbox "C" $cCode "C-Lang" "C execution: Hello C-Lang"

# 5. C++
$cppCode = @"
#include <iostream>
#include <vector>
#include <string>
#include <numeric>

template<typename T>
T sum_elements(const std::vector<T>& vec) {
    return std::accumulate(vec.begin(), vec.end(), T(0));
}

int main() {
    std::string name;
    if (std::cin >> name) {
        std::vector<int> nums = {1, 2, 3, 4, 5};
        int total = sum_elements(nums);
        std::cout << "C++ execution: " << name << " sum is " << total << std::endl;
    }
    return 0;
}
"@
$cppPassed = Test-Language-Sandbox "CPP" $cppCode "CPP-Lang" "C++ execution: CPP-Lang sum is 15"

# 6. Go
$goCode = @"
package main

import (
	"fmt"
	"io"
	"os"
	"strings"
)

func main() {
	bytes, err := io.ReadAll(os.Stdin)
	if err != nil {
		fmt.Println("Error reading stdin:", err)
		return
	}
	input := strings.TrimSpace(string(bytes))

	ch := make(chan string)
	go func() {
		ch <- "Go execution: " + input
	}()

	fmt.Println(<-ch)
}
"@
$goPassed = Test-Language-Sandbox "GO" $goCode "Golang" "Go execution: Golang"

Write-Host "=============================================" -ForegroundColor Cyan
Write-Host "Final Sandbox Execution Results Summary:" -ForegroundColor Cyan
Write-Host "Java:       $($javaPassed)"
Write-Host "Python:     $($pythonPassed)"
Write-Host "JavaScript: $($jsPassed)"
Write-Host "C:          $($cPassed)"
Write-Host "C++:        $($cppPassed)"
Write-Host "Go:         $($goPassed)"
Write-Host "=============================================" -ForegroundColor Cyan

if ($javaPassed -and $pythonPassed -and $jsPassed -and $cPassed -and $cppPassed -and $goPassed) {
    Write-Host "ALL LANGUAGES RUNTIME SANDBOX SUCCESSFULLY VERIFIED!" -ForegroundColor Green
} else {
    Write-Host "SOME SANDBOX RUNTIME LANGUAGES FAILED!" -ForegroundColor Red
    exit 1
}
