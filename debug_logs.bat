@echo off
set "ADB_PATH=c:\@MIS PROYECTOS\K706_RE\tools\platform-tools\adb.exe"
set "IP=192.168.1.78:9876"

echo ========================================
echo   OpenRadioFM - Debugger (Ruta Manual)
echo ========================================
echo.

if not exist "%ADB_PATH%" (
    echo [ERROR] No se encuentra adb.exe en:
    echo %ADB_PATH%
    pause
    exit /b
)

echo [OK] ADB encontrado en la carpeta de herramientas.
echo.
echo 1. Conectando a %IP%...
"%ADB_PATH%" disconnect %IP% >nul 2>nul
"%ADB_PATH%" connect %IP%
echo.
echo 2. Iniciando captura de logs (MainActivity y Supabase)...
echo [Presiona Ctrl+C para detener]
echo.
"%ADB_PATH%" logcat -v time OpenRadioFm:D SupabaseLogoSource:D RadioLogos:D *:S
echo.
echo Captura finalizada.
pause
