@echo off
echo =========================================
echo    LsLife V6.0 APK Automated Build
echo =========================================

set JAVA_HOME=C:\Program Files\Android\Android Studio\jbr
cd /d D:\LsLife\android

echo [1/3] Configuring build environment...
echo [2/3] Compiling Release APK (1-3 minutes)...
call gradlew.bat assembleRelease

echo [3/3] Checking build artifacts...
if exist "D:\LsLife\releases\*.apk" (
    echo.
    echo =========================================
    echo  [SUCCESS] Build Completed!
    echo  Location: D:\LsLife\releases\
    echo =========================================
    explorer "D:\LsLife\releases"
) else (
    echo.
    echo [ERROR] Build may have encountered an error.
)

pause
