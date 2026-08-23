$ErrorActionPreference = "Stop"
Write-Host "========================================="
Write-Host "   LsLife V6.0 APK Automated Build       "
Write-Host "========================================="

$projectDir = "D:\LsLife\android"
$releaseDir = "D:\LsLife\releases"

Write-Host "1. Configuring build environment..."
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
Set-Location -Path $projectDir

Write-Host "2. Starting Gradle AssembleRelease..."
.\gradlew.bat assembleRelease

Write-Host "3. Checking build artifacts..."
if (Test-Path "$releaseDir\*.apk") {
    Write-Host "SUCCESS! Build Completed." -ForegroundColor Green
    Write-Host "Location: $releaseDir\" -ForegroundColor Green
    Invoke-Item $releaseDir
} else {
    Write-Host "ERROR! Build failed or artifact not found." -ForegroundColor Red
}

Write-Host "Press any key to exit..."
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
