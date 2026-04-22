@echo off
setlocal enabledelayedexpansion

REM Genera un ZIP instalable de Magisk (K706_Root) de forma determinista,
REM evitando problemas de CRLF de Windows (Magisk App puede fallar si hay CRLF).

REM Moverse a la raíz del repo (magisk\ -> repo\)
cd /d "%~dp0.."
if errorlevel 1 (
  echo [ERROR] No se pudo cambiar al directorio del repo.
  exit /b 1
)

REM Ruta de salida (por defecto, dentro de magisk\)
set "OUT_ZIP=%~dp0k706.zip"

echo [INFO] Repo: %CD%
echo [INFO] Output: %OUT_ZIP%
echo.

REM Asegurar rama K706_Root (sin forzar)
for /f "delims=" %%b in ('git branch --show-current 2^>nul') do set "CUR_BRANCH=%%b"
if not defined CUR_BRANCH (
  echo [ERROR] No se pudo leer la rama actual. ^(git no disponible?^)
  exit /b 1
)

if /i not "!CUR_BRANCH!"=="K706_Root" (
  echo [WARN] Estas en la rama "!CUR_BRANCH!". Recomendado: K706_Root
  echo [WARN] Continuo igualmente para crear el ZIP desde la rama actual.
)

REM Construir zip desde el árbol git (magisk/K706_Root)
if exist "%OUT_ZIP%" del /f /q "%OUT_ZIP%" >nul 2>&1

git archive --format=zip -o "%OUT_ZIP%" HEAD:magisk/K706_Root
if errorlevel 1 (
  echo [ERROR] git archive fallo.
  exit /b 1
)

for %%I in ("%OUT_ZIP%") do (
  echo.
  echo [OK] ZIP generado: %%~fI
  echo [OK] Tamano: %%~zI bytes
)

echo.
echo [NEXT] Copia el ZIP a la radio e instalalo en Magisk: Modules ^> Install from storage
echo [NEXT] Si te falla la app, instala por ADB:
echo        adb push "%OUT_ZIP%" /sdcard/Download/k706.zip
echo        adb shell su -c "magisk --install-module /sdcard/Download/k706.zip"
echo        adb shell su -c "reboot"
echo.
pause

