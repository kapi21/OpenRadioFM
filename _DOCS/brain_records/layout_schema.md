# Esquema de Layouts y IDs - OpenRadioFM

Este documento sirve como mapa de referencia para identificar todos los elementos de la interfaz de usuario por sus IDs.

## 📱 Pantallas Principales (Activities)

### [activity_main.xml](file:///c:/@MIS%20PROYECTOS/K706_RE/OpenRadioFM/app/src/main/res/layout/activity_main.xml) (Modo Clásico)
- **Contenedor Raíz**: `rootLayout` (ConstraintLayout)
- **Fondo Dinámico**: `ivDynamicBackground` (ImageView)
- **Presintonías (Izquierda)**:
  - Scroll: `scrollViewPresets`
  - Cards: `cardP1` a `cardP12` (LinearLayout)
  - Iconos: `ivP1` a `ivP12` (ImageView)
  - Frecuencias: `tvP1` a `tvP12` (TextView)
- **Centro (Información)**:
  - Caja Frecuencia: `boxFrequency` (FrameLayout)
  - Indicador de Banda: `ivBandIndicator` (ImageView)
  - Icono Estéreo: `ivStereoIcon` (ImageView)
  - Texto Frecuencia: `tvFrequency` (TextView)
  - Nivel Señal: `ivSignalLevel` (ImageView)
  - Etiqueta Unidad (MHz): `ivUnitLabel` (ImageView)
  - Texto PTY: `tvPty` (TextView)
  - Icono PTY: `ivPtyIcon` (ImageView)
  - Indicador Favorito: `ivFavoriteIndicator` (ImageView)
  - Iconos RDS (AF/TA/TP): `ivAfIcon`, `ivTaIcon`, `ivTpIcon` (ImageView)
- **Controles Centrales**:
  - Búsqueda: `btnSeekDown`, `btnSeekUp` (ImageButton)
  - Navegar Favoritos: `btnFavPrev`, `btnFavNext` (ImageButton)
  - RDS: `tvRdsName` (Nombre), `tvRdsInfo` (RadioText) (TextView)
  - Banda/Escaneo: `btnBand`, `btnAutoScan` (ImageButton)
- **Derecha (Extras)**:
  - Logo Principal: `ivMainLogo`
  - Android Settings: `btnExtra1`
  - Guardar/Cargar: `btnExtra2`
  - Loc/Dx: `btnLocDx`
  - Mute: `btnMute`
  - Ecualizador/Ajustes: `btnSettings`
  - GPS: `btnGps`

### [activity_main_v3.xml](file:///c:/@MIS%20PROYECTOS/K706_RE/OpenRadioFM/app/src/main/res/layout/activity_main_v3.xml) (Modo Horizontal/V3)
*Similar a Classic pero con disposición horizontal.*
- **Controles Inferiores**: `bottomControls` (LinearLayout) conteniendo `btnSettings`, `btnBand`, `btnLocDx`, `btnAutoScan`, `btnMute`, `btnGps`, `btnExtra1`, `btnExtra2`.
- **Galería de Favoritos**: `cardP1` a `cardP12` dentro de un `HorizontalScrollView`.

---

## 🛠️ Diálogos de Configuración

### [dialog_premium_settings.xml](file:///c:/@MIS%20PROYECTOS/K706_RE/OpenRadioFM/app/src/main/res/layout/dialog_premium_settings.xml)
- **Personalización**:
  - Botón Tema: `cardTheme`
  - Previsualización Color: `viewColorPreview`
  - Botón Fuentes: `cardFonts`
  - Previsualización Fuente: `tvFontPreview`
  - Botón Fondo: `cardBackground`
  - Estado Fondo: `tvBackgroundStatus`
- **Opciones de App**:
  - Selector Idioma: `rowLanguage`
  - Idioma Actual: `tvCurrentLanguage`
  - Logos Online: `switchLogosOnline` (Switch)
  - Barra Estado V2: `switchStatusBarV2` (Switch)
  - Modo Noche Auto: `switchNightMode` (Switch)
  - Horario Noche: `rowNightSchedule`, `tvNightStart`, `tvNightEnd`
  - Guardar Historial: `switchSaveHistory` (Switch)
- **Hardware**:
  - banda AM: `switchEnableAm` (Switch)
  - Radio Engine: `rowEngine`, `tvCurrentEngine`
  - Gestos (Beta): `switchSwipeGestures` (Switch)
- **General**:
  - Botón Acerca de: `btnAbout`
  - Botón Cerrar: `btnCloseSettings`

### [dialog_save_load.xml](file:///c:/@MIS%20PROYECTOS/K706_RE/OpenRadioFM/app/src/main/res/layout/dialog_save_load.xml)
- **Acciones**:
  - Guardar Favoritos (.fav): `btnSave`
  - Cargar Favoritos (.fav): `btnLoad`
  - Borrar TODO: `btnDeleteAllFavs`
  - Borrar Historial: `btnClearHistory`
  - Cerrar/Cancelar: `btnClose`

---

## 📋 Información y Otros

### [dialog_credits.xml](file:///c:/@MIS%20PROYECTOS/K706_RE/OpenRadioFM/app/src/main/res/layout/dialog_credits.xml)
- **Imagen Hacker**: `ivHackerImage`
- **Versión**: `tvVersionCredits`
- **Cierre**: `btnOkCredits`

### [dialog_about.xml](file:///c:/@MIS%20PROYECTOS/K706_RE/OpenRadioFM/app/src/main/res/layout/dialog_about.xml)
- **Versión App**: `tvAppVersion`
- **Crédito Iconos**: `tvIcons8Credit`
- **Cerrar**: `btnClose`

### [dialog_selective_scan.xml](file:///c:/@MIS%20PROYECTOS/K706_RE/OpenRadioFM/app/src/main/res/layout/dialog_selective_scan.xml)
- **Título**: `tvScanTitle`
- **Frecuencia Actual**: `tvCurrentScanFreq`
- **Estado RDS**: `tvScanStatus`
- **Lista de Estaciones**: `rvCapturedStations` (RecyclerView)
- **Botones**: `btnStopScan`, `btnNextScan`

---

## 👨‍💻 Modo Ingeniería (Avanzado)

### [dialog_engineering_mode.xml](file:///c:/@MIS%20PROYECTOS/K706_RE/OpenRadioFM/app/src/main/res/layout/dialog_engineering_mode.xml) (MT8163)
- **Cabeceras**: `tvHeader`, `tvSubHeader`
- **RF Telemetry**: `tvSignalQualityIndex`, `tvStereoPilot`, `tvTunerMode`, `tvRssiBar`
- **Tuner Raw**: `btnTuneDown`, `btnTuneUp`
- **RDS Debug**: `tvPiCode`, `tvPtyRaw`, `tvRdsSync`, `tvAfList`
- **Salud del Sistema**: `tvServiceLatency`, `tvMemoryUsage`, `tvChipset`, `tvDeviceInfo`, `tvRootStatus`
- **Gestión de Archivos**: `tvAssetsInfo`
- **Resets**: `btnResetFavs`, `btnResetHistory`
- **Logs**: `tvTerminalLog`
- **Cerrar**: `btnCloseEng`, `btnExitSystem`

### [dialog_k706_engineering.xml](file:///c:/@MIS%20PROYECTOS/K706_RE/OpenRadioFM/app/src/main/res/layout/dialog_k706_engineering.xml) (K706)
- **Monitor**: `tvK706Monitor`
- **Comandos Sintonía**: `btnK706FineDown`, `btnK706FineUp`, `btnK706SeekDown`, `btnK706SeekUp`, `btnK706AutoScan`, `btnK706StopScan`, `btnK706SavePreset`
- **Configuración**: `btnK706Band`, `btnK706LocDx`, `btnK706Area0`, `btnK706Area1`, `btnK706PtyReset`, `btnK706PtyNews`
- **Canales Audio**: `btnK706Ch1` (BT), `btnK706Ch2` (FM), `btnK706Ch3` (AUX), `btnK706Ch4` (AND)
- **Log Raw**: `tvK706Log`
- **Cerrar**: `btnCloseK706`, `btnK706Exit`
