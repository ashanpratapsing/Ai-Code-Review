Write-Host "================================" -ForegroundColor Cyan
Write-Host "   Antigravity API Health Check" -ForegroundColor Cyan
Write-Host "================================" -ForegroundColor Cyan

$baseUrl = "http://localhost:8080"

function Test-Endpoint($url, $name) {
    try {
        $response = Invoke-RestMethod -Uri $url -Method Get -ErrorAction Stop
        Write-Host "[PASS] $name is responding at $url" -ForegroundColor Green
        return $response
    } catch {
        Write-Host "[FAIL] $name failed at $url. Is the backend running?" -ForegroundColor Red
        return $null
    }
}

Write-Host "`nChecking core endpoints..."
$summary = Test-Endpoint "$baseUrl/dashboard/summary" "Dashboard Service"

if ($summary) {
    Write-Host "`nSystem Stats:"
    Write-Host " - Total Projects: $($summary.totalProjects)"
    Write-Host " - Total Files: $($summary.totalFiles)"
    Write-Host " - AI Reports Gen: $($summary.totalAIReports)"
}

Write-Host "`nNote: Auth endpoints require POST data and are not tested by this script."
Write-Host "Use the ANTIGRAVITY_GUIDE.md for full manual testing steps."
