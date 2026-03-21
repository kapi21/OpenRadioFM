@echo off
chcp 65001 >nul
REM =============================================================================
REM Consulta paquetes NWD en la unidad (Windows).
REM
REM Opción A — Ejecutar desde la carpeta donde está adb.exe (p. ej. platform-tools):
REM   copia este .bat allí y:  nwd_pm_info.bat
REM
REM Opción B — adb.exe en la misma carpeta que este script:
REM   coloca adb.exe junto a nwd_pm_info.bat
REM
REM Opción C — Variable ADB apuntando al ejecutable:
REM   set ADB=C:\ruta\al\sdk\platform-tools\adb.exe
REM   nwd_pm_info.bat
REM
REM APKs de la unidad (pull) guardar en:
REM   C:\@MIS PROYECTOS\K706_RE\QS NWD\tools\
REM (ver docs\NWD_FASE_A_MAPA_SISTEMA.md §A.0)
REM =============================================================================

set "SCRIPT_DIR=%~dp0"
set "ADB=%ADB%"

if "%ADB%"=="" if exist "%SCRIPT_DIR%adb.exe" set "ADB=%SCRIPT_DIR%adb.exe"
if "%ADB%"=="" if exist "%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe" (
    set "ADB=%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe"
)
if "%ADB%"=="" set "ADB=adb"

echo === ADB: %ADB% ===
echo.

"%ADB%" shell pm list packages | findstr nwd
echo.

"%ADB%" shell pm path com.nwd.radio
echo.

"%ADB%" shell pm path com.nwd.radio.service
echo.

"%ADB%" shell pm path com.nwd.kernel
echo.

"%ADB%" shell dumpsys package com.nwd.radio | findstr version
echo.

"%ADB%" shell dumpsys package com.nwd.radio.service | findstr version
echo.

"%ADB%" shell dumpsys package com.nwd.kernel | findstr version
echo.

pause
