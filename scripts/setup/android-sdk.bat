@echo off
setlocal

:: ==============================================================================
:: Life OS — Android SDK Auto-config (Windows)
:: ==============================================================================

set "ANDROID_DIR=android"
set "PROPERTIES_FILE=%ANDROID_DIR%\local.properties"

echo Detecting OS...
echo Windows detected.

:: Standard Windows SDK path
set "SDK_PATH=C:\Users\%USERNAME%\AppData\Local\Android\Sdk"

echo Writing sdk.dir to %PROPERTIES_FILE%...
echo sdk.dir=%SDK_PATH% > "%PROPERTIES_FILE%"

echo Done.
pause
