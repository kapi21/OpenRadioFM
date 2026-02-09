# Script para crear Releases en GitHub - OpenRadioFM

## Paso 1: Crear los tags de Git

Ejecuta estos comandos en tu terminal (Git Bash o PowerShell):

```bash
cd "d:\@MIS PROYECTOS\OpenRadioFM"

# Crear tags para cada versión
git tag -a v1.0 -m "OpenRadioFM v1.0b - Primera versión beta"
git tag -a v2.0 -m "OpenRadioFM v2.0b - Expansión de memorias y personalización"
git tag -a v3.0 -m "OpenRadioFM v3.0 - The Car Experience"

# Subir los tags a GitHub
git push origin --tags
```

## Paso 2: Crear los Releases en GitHub (Manual)

1. Ve a: https://github.com/kapi21/OpenRadioFM/releases
2. Click en **"Draft a new release"**

### Release v1.0
- **Tag**: v1.0
- **Title**: OpenRadioFM v1.0b
- **Description**:
```markdown
Primera versión beta de OpenRadioFM.

## Características
- Interfaz básica de radio FM
- 6 presets por banda
- Soporte para logos personalizados

## Instalación
Compatible con unidades Android Head MT8163, ESSGO y JUNSU V1 8581a.
```
- **Archivo**: Sube `_RELEASES/OpenRadioFM v1b.apk`

### Release v2.0
- **Tag**: v2.0
- **Title**: OpenRadioFM v2.0b
- **Description**:
```markdown
## Novedades v2.0b

### 💾 Mejoras
- **Expansión de Memorias**: De 6 a 12 presets por banda
- **Personalización de Fondos**: Soporte para `background.jpg/png`
- **Interfaz Glassmorphism**: Nuevos botones semitransparentes

## Instalación
Compatible con unidades Android Head MT8163, ESSGO y JUNSU V1 8581a.
```
- **Archivo**: Sube `_RELEASES/OpenRadioFM v2b.apk`

### Release v3.0
- **Tag**: v3.0
- **Title**: OpenRadioFM v3.0 - The Car Experience
- **Description**:
```markdown
## 🚀 Novedades v3.0 "The Car Experience"

La actualización más ambiciosa hasta la fecha.

### Principales características
- **Nuevo Layout Horizontal (V3)**: Diseñado para pantallas 1024x600
- **Logo Marca Coche**: Soporte para logo personalizado
- **Indicadores de Banda Gráficos**: Iconos dinámicos FM1/FM2
- **Menú Premium**: Centro de personalización completo
- **Fondo Dinámico**: Cambia al logo de la emisora con efecto blur
- **5 Tipografías**: System, Bebas, Digital, Inter y Orbitron
- **Botón GPS**: Acceso rápido a navegación

### 🛠️ Mejoras técnicas
- Optimización para pantallas 1024x600
- Refresco en tiempo real de cambios
- Internacionalización completa (ES/EN/RU)

### 🐛 Bugs corregidos
- Persistencia de logos con MediaScanner
- Lógica SEEK corregida
- Modo pantalla completa mejorado

## Instalación
Compatible con unidades Android Head MT8163, ESSGO y JUNSU V1 8581a.
```
- **Archivo**: Sube `_RELEASES/OpenRadioFM v3.0.apk`

## Paso 3: Verificar

Una vez creados, los releases aparecerán en:
https://github.com/kapi21/OpenRadioFM/releases

Y el enlace de la web ya funcionará correctamente apuntando al CHANGELOG.md
