@echo off
setlocal enabledelayedexpansion

REM Genera un ZIP instalable de Magisk (K706_Root) de forma determinista.
REM IMPORTANTE: Normaliza a LF los scripts dentro del ZIP, porque Magisk App
REM puede fallar con "Installation failed" si los .sh/update-binary van en CRLF.

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

REM Construir zip desde los archivos del módulo, normalizando LF en una carpeta temporal.
if exist "%OUT_ZIP%" del /f /q "%OUT_ZIP%" >nul 2>&1

set "TMP_DIR=%TEMP%\\orf_magisk_k706_root"
if exist "%TMP_DIR%" rmdir /s /q "%TMP_DIR%" >nul 2>&1
mkdir "%TMP_DIR%" >nul 2>&1
if errorlevel 1 (
  echo [ERROR] No se pudo crear carpeta temporal: %TMP_DIR%
  exit /b 1
)

REM Copiar estructura del módulo
xcopy /E /I /Y "magisk\\K706_Root\\META-INF" "%TMP_DIR%\\META-INF" >nul
copy /Y "magisk\\K706_Root\\module.prop" "%TMP_DIR%\\module.prop" >nul
copy /Y "magisk\\K706_Root\\customize.sh" "%TMP_DIR%\\customize.sh" >nul
copy /Y "magisk\\K706_Root\\service.sh" "%TMP_DIR%\\service.sh" >nul
copy /Y "magisk\\K706_Root\\uninstall.sh" "%TMP_DIR%\\uninstall.sh" >nul

REM Normalizar LF (reemplazar CRLF->LF) en scripts y update-binary
powershell -NoProfile -ExecutionPolicy Bypass -Command ^
  "& { $files = @(" ^
  "'%TMP_DIR%\\customize.sh'," ^
  "'%TMP_DIR%\\service.sh'," ^
  "'%TMP_DIR%\\uninstall.sh'," ^
  "'%TMP_DIR%\\META-INF\\com\\google\\android\\update-binary'," ^
  "'%TMP_DIR%\\META-INF\\com\\google\\android\\updater-script'" ^
  "); foreach($f in $files){ if(Test-Path $f){ $b=[IO.File]::ReadAllBytes($f); $b2 = New-Object byte[] ($b.Length); [Array]::Copy($b,$b2,$b.Length); " ^
  "$s=[Text.Encoding]::UTF8.GetString($b2); $s=$s -replace \"\\r\\n\",\"\\n\"; $out=[Text.Encoding]::UTF8.GetBytes($s); [IO.File]::WriteAllBytes($f,$out) } } }"
if errorlevel 1 (
  echo [ERROR] Fallo normalizando LF.
  exit /b 1
)

REM Crear ZIP con PowerShell (sin cifrado)
powershell -NoProfile -ExecutionPolicy Bypass -Command ^
  "& { if(Test-Path '%OUT_ZIP%'){ Remove-Item -Force '%OUT_ZIP%' }; Compress-Archive -Path '%TMP_DIR%\\*' -DestinationPath '%OUT_ZIP%' -Force }"
if errorlevel 1 (
  echo [ERROR] Fallo creando ZIP con Compress-Archive.
  exit /b 1
)

REM Limpiar temp
rmdir /s /q "%TMP_DIR%" >nul 2>&1

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

