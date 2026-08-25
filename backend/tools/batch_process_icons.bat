@echo off
setlocal enabledelayedexpansion

echo ========================================================
echo LianShan Image Processing Pipeline Tool Batch Script
echo ========================================================
echo.

set PYTHON_CMD=python
set SCRIPT_PATH=%~dp0process_icon.py

:: Check if Python is installed
%PYTHON_CMD% --version >nul 2>&1
if %errorlevel% neq 0 (
    echo [Error] Python is not installed or not in PATH.
    pause
    exit /b 1
)

:: Check if rembg is installed
%PYTHON_CMD% -c "import rembg" >nul 2>&1
if %errorlevel% neq 0 (
    echo [Warning] 'rembg' is not installed. Background removal will fail if method is 'rembg'.
    echo You can install it via: pip install rembg
    echo.
)

set /p INPUT_PATH="Enter input directory or file path: "
if "%INPUT_PATH%"=="" (
    echo [Error] Input path cannot be empty.
    pause
    exit /b 1
)

set /p OUTPUT_PATH="Enter output directory (default: output_icons): "
if "%OUTPUT_PATH%"=="" set OUTPUT_PATH=output_icons

set /p METHOD="Enter processing method [rembg/floodfill/none] (default: rembg): "
if "%METHOD%"=="" set METHOD=rembg

echo.
echo Running: %PYTHON_CMD% "%SCRIPT_PATH%" -i "%INPUT_PATH%" -o "%OUTPUT_PATH%" --method %METHOD%
echo.

%PYTHON_CMD% "%SCRIPT_PATH%" -i "%INPUT_PATH%" -o "%OUTPUT_PATH%" --method %METHOD%

echo.
echo Batch processing finished.
pause
