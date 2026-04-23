@echo off
setlocal enabledelayedexpansion

REM ZIP Magisk FAKE para diagnosticar Installation failed (sin system overlay).
cd /d "%~dp0.."
if errorlevel 1 (
  echo [ERROR] No se pudo cambiar al directorio del repo.
  exit /b 1
)

set "OUT_ZIP=%~dp0k706_fake.zip"
set "TMP_DIR=%TEMP%\\orf_magisk_k706_fake"

if exist "%OUT_ZIP%" del /f /q "%OUT_ZIP%" >nul 2>&1
if exist "%TMP_DIR%" rmdir /s /q "%TMP_DIR%" >nul 2>&1
mkdir "%TMP_DIR%" >nul 2>&1
if errorlevel 1 (
  echo [ERROR] No se pudo crear carpeta temporal: %TMP_DIR%
  exit /b 1
)

xcopy /E /I /Y "magisk\\K706_Root_Fake\\META-INF" "%TMP_DIR%\\META-INF" >nul
copy /Y "magisk\\K706_Root_Fake\\module.prop" "%TMP_DIR%\\module.prop" >nul
copy /Y "magisk\\K706_Root_Fake\\customize.sh" "%TMP_DIR%\\customize.sh" >nul
copy /Y "magisk\\K706_Root_Fake\\service.sh" "%TMP_DIR%\\service.sh" >nul
copy /Y "magisk\\K706_Root_Fake\\uninstall.sh" "%TMP_DIR%\\uninstall.sh" >nul

REM Normalizar LF
powershell -NoProfile -ExecutionPolicy Bypass -Command ^
  "& { $files = @(" ^
  "'%TMP_DIR%\\customize.sh'," ^
  "'%TMP_DIR%\\service.sh'," ^
  "'%TMP_DIR%\\uninstall.sh'," ^
  "'%TMP_DIR%\\META-INF\\com\\google\\android\\update-binary'," ^
  "'%TMP_DIR%\\META-INF\\com\\google\\android\\updater-script'" ^
  "); foreach($f in $files){ if(Test-Path $f){ $b=[IO.File]::ReadAllBytes($f); $s=[Text.Encoding]::UTF8.GetString($b); $s=$s -replace \"\\r\\n\",\"\\n\"; [IO.File]::WriteAllBytes($f,[Text.Encoding]::UTF8.GetBytes($s)) } } }"
if errorlevel 1 (
  echo [ERROR] Fallo normalizando LF.
  exit /b 1
)

set "PY_ZIPPER=%CD%\\magisk\\zip_magisk.py"
if exist "%PY_ZIPPER%" goto :zip_with_python
goto :zip_with_powershell

:zip_with_python
python "%PY_ZIPPER%" "%TMP_DIR%" "%OUT_ZIP%"
if errorlevel 1 (
  echo [ERROR] Fallo creando ZIP con Python.
  exit /b 1
)
goto :zip_done

:zip_with_powershell
echo [WARN] No existe %PY_ZIPPER%. Usando Compress-Archive (puede fallar en Magisk por permisos).
powershell -NoProfile -ExecutionPolicy Bypass -Command ^
  "& { if(Test-Path '%OUT_ZIP%'){ Remove-Item -Force '%OUT_ZIP%' }; Compress-Archive -Path '%TMP_DIR%\\*' -DestinationPath '%OUT_ZIP%' -Force }"
if errorlevel 1 (
  echo [ERROR] Fallo creando ZIP con Compress-Archive.
  exit /b 1
)

:zip_done
rmdir /s /q "%TMP_DIR%" >nul 2>&1

for %%I in ("%OUT_ZIP%") do (
  echo [OK] ZIP FAKE generado: %%~fI
  echo [OK] Tamano: %%~zI bytes
)

echo [NEXT] Instala k706_fake.zip desde Magisk App (Modules ^> Install from storage)
echo [NEXT] Tras reiniciar, revisa /data/local/tmp/orf_k706_fake_magisk.log

