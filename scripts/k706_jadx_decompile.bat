@echo off
REM =============================================================================
REM Decompila la radio OEM K706 con jadx (solo paquetes FM / QuickFish).
REM
REM jadx 1.5+ necesita JDK 11 o superior. Si tienes Java 8 en PATH, usa una de:
REM   1) Antes de ejecutar (CMD):
REM        set "J706_JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.x.x-hotspot"
REM   2) O configura JAVA_HOME al JDK 17 (Panel de control - Variables de entorno).
REM
REM Coloca jadx en:  ..\K706_RE\jadx\  (hermano de OpenRadioFM)
REM   o define JADX_HOME apuntando a la carpeta "jadx".
REM
REM Uso:
REM   k706_jadx_decompile.bat [ruta\Radio_Original.apk]
REM =============================================================================
setlocal EnableDelayedExpansion

set "SCRIPT_DIR=%~dp0"
set "DEFAULT_APK=%SCRIPT_DIR%..\..\K706_RE\Radio_Original.apk"

REM --- Forzar JDK 17+ para jadx (jadx.bat usa JAVA_HOME si existe) ---
if defined J706_JAVA_HOME (
  set "JAVA_HOME=%J706_JAVA_HOME%"
  set "PATH=%JAVA_HOME%\bin;%PATH%"
)

REM Comprobar version de java que usara jadx
set "JAVA_CHECK=%JAVA_HOME%\bin\java.exe"
if not defined JAVA_HOME set "JAVA_CHECK=java"

echo [Java] Comprobando: "%JAVA_CHECK%"
"%JAVA_CHECK%" -version 2>&1
echo.

REM Detectar Java 8 (salida tipica: java version "1.8.0_xxx")
"%JAVA_CHECK%" -version 2>&1 | findstr /C:"1.8.0" /C:"\"1.8." >nul
if %ERRORLEVEL% equ 0 (
  echo [ERROR] Este script detecto Java 8. jadx necesita JDK 11+.
  echo.
  echo Solucion — abre CMD y ejecuta ANTES de este script ^(ajusta la ruta^):
  echo   set "J706_JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.x.x-hotspot"
  echo   set "PATH=%%J706_JAVA_HOME%%\bin;%%PATH%%"
  echo   k706_jadx_decompile.bat
  echo.
  echo O bien define J706_JAVA_HOME en variables de entorno de usuario.
  exit /b 2
)

if defined JADX_HOME (
  set "JADX_BAT=%JADX_HOME%\bin\jadx.bat"
) else (
  set "JADX_BAT=%SCRIPT_DIR%..\..\K706_RE\jadx\bin\jadx.bat"
)

if not exist "%JADX_BAT%" (
  echo [ERROR] No se encuentra jadx: "%JADX_BAT%"
  echo Define JADX_HOME o coloca jadx en ..\K706_RE\jadx\ respecto a OpenRadioFM
  exit /b 1
)

if not "%~1"=="" (
  set "APK=%~1"
) else if defined K706_RADIO_APK (
  set "APK=%K706_RADIO_APK%"
) else (
  set "APK=%DEFAULT_APK%"
)

if not exist "%APK%" (
  echo [ERROR] APK no encontrada: "%APK%"
  echo Copia Radio_Original.apk o pasa la ruta como argumento.
  exit /b 1
)

set "OUT_DIR=%SCRIPT_DIR%..\..\K706_RE\jadx_out_radio_oem"
if defined K706_JADX_OUT set "OUT_DIR=%K706_JADX_OUT%"

echo JADX:  "%JADX_BAT%"
echo APK:   "%APK%"
echo OUT:   "%OUT_DIR%"
echo.

call "%JADX_BAT%" ^
  -d "%OUT_DIR%" ^
  --show-bad-code ^
  --select-package com.android.fmradio ^
  --select-package com.qf.clientsdk ^
  "%APK%"

set "EC=!ERRORLEVEL!"
if !EC! neq 0 (
  echo.
  echo [ERROR] jadx termino con codigo !EC!
  echo Si el mensaje fue UnsupportedClassVersionError, usa J706_JAVA_HOME con JDK 17.
  exit /b !EC!
)

echo.
echo Listo. Fuentes Java en: "%OUT_DIR%"
exit /b 0
