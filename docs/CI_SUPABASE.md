# Credenciales Supabase en compilación

La aplicación **no** incluye la URL ni la clave anónima de Supabase en el código fuente. Gradle las necesita para generar `BuildConfig` y compilar.

## Desarrollo local

1. Copia `local.properties.example` sobre `local.properties` en la **raíz del repo** (junto a `settings.gradle.kts`).
2. Rellena `SUPABASE_URL` y `SUPABASE_ANON_KEY` con los valores de **Project Settings → API** en el panel de Supabase.
3. Mantén `sdk.dir=...` si ya lo generó el IDE.

`local.properties` no debe subirse a Git (está en `.gitignore`).

## CI (GitHub Actions, GitLab CI, etc.)

Define secretos del repositorio, por ejemplo:

- `SUPABASE_URL` → `https://xxxx.supabase.co/` (con o sin barra final; el build la normaliza)
- `SUPABASE_ANON_KEY` → la clave **anon** **public**

### Ejemplo (GitHub Actions)

```yaml
env:
  SUPABASE_URL: ${{ secrets.SUPABASE_URL }}
  SUPABASE_ANON_KEY: ${{ secrets.SUPABASE_ANON_KEY }}

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: 'temurin'
          java-version: '17'
      - run: ./gradlew assembleRelease
```

Gradle lee **variables de entorno** con esos nombres; no hace falta crear `local.properties` en el runner si el entorno está definido.

### Línea de comandos (prueba local)

PowerShell:

```powershell
$env:SUPABASE_URL="https://xxxx.supabase.co/"
$env:SUPABASE_ANON_KEY="eyJ..."
.\gradlew.bat assembleDebug
```

## Rotación de clave en Supabase

Si regeneras la clave anónima en el panel, las APK ya publicadas seguirán usando la clave **antigua** hasta que los usuarios actualicen. Planifica la rotación o mantén compatibilidad según tu política de versiones.
