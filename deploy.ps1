
$ErrorActionPreference = "Stop"

# Project paths
$root = $PSScriptRoot
$apkSource = Join-Path $root "VacationScheduler\app\build\outputs\apk\release\app-release.apk"
$apkDestination = Join-Path $root "hosting\VacationScheduler.apk"

# Check that the signed APK exists
if (-not (Test-Path $apkSource)) {
    Write-Host "Signed APK not found. Generate a release APK first."
    exit 1
}

# Copy APK into Firebase Hosting folder
Copy-Item $apkSource $apkDestination -Force
Write-Host "APK copied to Hosting folder."

# Deploy to Firebase
Set-Location $root
firebase deploy --only hosting --project vacation-scheduler-d46f7

if ($LASTEXITCODE -ne 0) {
    Write-Host "Firebase deployment failed."
    exit 1
}

Write-Host "Firebase deployment completed successfully!"
