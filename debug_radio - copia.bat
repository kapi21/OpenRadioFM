@echo off
set ADB="d:\@MIS PROYECTOS\K706_RE\tools\platform-tools\adb.exe"

echo ============================================
echo  OpenRadioFM K706 Debug Logger V7.2
echo ============================================
echo.
echo Conectando a K706 via ADB...
%ADB% connect 192.168.1.39:9876
echo.
echo Limpiando buffer de logcat...
%ADB% logcat -c
echo.
echo Capturando logs en: radio_debug_v72.log
echo Pulsa Ctrl+C para detener la captura.
echo.
%ADB% logcat -v time -s K706RadioManager:D mcu_services:E TunerPresenter:D TunerManagerForExt:D FmService:D McuManager:E > radio_debug_v72.log
echo.
echo Log guardado en radio_debug_v72.log
pause
