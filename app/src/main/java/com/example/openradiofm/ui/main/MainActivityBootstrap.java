package com.example.openradiofm.ui.main;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.openradiofm.R;

/**
 * Secuencia de inicialización de {@link MainActivity#onCreate} tras {@code super.onCreate}
 * (Fase 1 refactor 5.2.0.MCU).
 */
final class MainActivityBootstrap {

    private MainActivityBootstrap() {}

    static void runAfterSuper(MainActivity a, Bundle savedInstanceState) {
        logLaunchIntent(a);
        createEarlyCoordinators(a);
        configureVolumeStream(a);
        restoreInstanceState(a, savedInstanceState);
        initHardwareAndWidgetBridge(a);
        initPrefsLayoutFlagsAndStatusBar(a);
        inflateContentAndUiController(a);
        bindSignalMeter(a);
        ensureFirstRunLanguageCountry(a);
        initUiControllerViewsFontsIcons(a);
        maybeRequestStoragePermission(a);
        initLogoServiceAndPlaybackStack(a);
        loadLogoAssetsAndSimpleLayoutManager(a);
        bootstrapLastFrequencyAndBands(a);
        initSimpleLayoutIfNeeded(a);
        bindMainFrequencyViewsAndClock(a);
        applyLogoModeBindIndicatorsAndStreaming(a);
        wireRdsIconsAndStereoToggle(a);
        wireCarLogoAndControlPanel(a);
        initThemeSkinNightAndAutoHideV3(a);
        finalizeBootstrap(a);
    }

    private static void logLaunchIntent(MainActivity a) {
        try {
            android.content.Intent i = a.getIntent();
            Log.i(MainActivity.TAG, "Intent(launch): action=" + (i != null ? i.getAction() : "null")
                    + " data=" + (i != null ? i.getDataString() : "null")
                    + " categories=" + (i != null ? i.getCategories() : "null")
                    + " extras=" + (i != null ? i.getExtras() : "null")
                    + " flags=0x" + (i != null ? Integer.toHexString(i.getFlags()) : "0"));
        } catch (Exception ignored) {}
    }

    private static void createEarlyCoordinators(MainActivity a) {
        a.mSkinCoordinator = new SkinCoordinator(a);
        a.mStatusRefreshCoordinator = new StatusRefreshCoordinator(a);
        a.mEngineCallbackCoordinator = new EngineCallbackCoordinator(a);
        a.mLifecycleCoordinator = new LifecycleCoordinator(a);
        a.mHardwareKeyCoordinator = new HardwareKeyCoordinator(a);
        a.mUiMediator = new UiViewMediator(a);
        a.mFreqStateManager = new FrequencyStateManager();
        a.mFrequencyChangeCoordinator = new FrequencyChangeCoordinator(a);
    }

    private static void configureVolumeStream(MainActivity a) {
        // V19.2: Forzar que el control de volumen por hardware afecte al stream de musica
        // desde el inicio. Esto evita el bug de doble pulsacion en MTK.
        a.setVolumeControlStream(android.media.AudioManager.STREAM_MUSIC);
    }

    private static void restoreInstanceState(MainActivity a, Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            a.mLastFreq = savedInstanceState.getInt("mLastFreq", -1);
            a.mIsV3 = savedInstanceState.getBoolean("mIsV3", false);
            a.mIsRecreating = true;
            Log.d(MainActivity.TAG, "State Restored: Freq=" + a.mLastFreq + " (Recreation detected)");
        }
    }

    private static void initHardwareAndWidgetBridge(MainActivity a) {
        // V18.6: MCU and BT logic controlled by HardwareManager
        a.mHardwareManager = new HardwareManager(a);
        a.mHardwareManager.registerReceivers();

        // V23.0: Gestor de broadcasts para widgets OEM
        a.mWidgetBroadcastManager = new WidgetBroadcastManager();
    }

    private static void initPrefsLayoutFlagsAndStatusBar(MainActivity a) {
        // V3.0: Layout Selection
        a.mPrefs = a.getSharedPreferences("RadioPresets", Context.MODE_PRIVATE); // Init prefs early
        a.mIconPackManager = new IconPackManager(a, a.mPrefs);
        a.mPresetNumberIconManager = new PresetNumberIconManager(a);

        // V21.3: Forzar habilitación de banda AM para evitar inestabilidad en motores HW (MTK8259)
        // Se ha eliminado la opción de desactivarlo en Ajustes Premium.
        if (!a.mPrefs.getBoolean("pref_enable_am", true)) {
            a.mPrefs.edit().putBoolean("pref_enable_am", true).apply();
            Log.i(MainActivity.TAG, "AM Band forced to enabled for stability.");
        }

        a.mIsV3 = a.mPrefs.getBoolean("pref_layout_v3", false);
        a.mIsSimpleLayout = a.mPrefs.getBoolean("pref_layout_simple", false);
        // Un solo layout activo: Simple gana. Si ambas prefs quedaron true (migración, backup, bug),
        // la UI es Simple pero a.mIsV3=true hacía que LogoManager ocultara a.mUiMediator.ivMainLogo como en V3.
        if (a.mIsSimpleLayout) {
            if (a.mIsV3) {
                a.mPrefs.edit().putBoolean("pref_layout_v3", false).apply();
            }
            a.mIsV3 = false;
        } else if (a.mIsV3) {
            a.mIsSimpleLayout = false;
        }

        // V4.8: Manejo de Barra de Estado (Fullscreen condicional)
        a.applyStatusBarVisibility();
    }

    private static void inflateContentAndUiController(MainActivity a) {
        if (a.mIsSimpleLayout) {
            a.setContentView(R.layout.activity_simple_radio);
            a.mUiController = new SimpleLayoutController(a);
        } else if (a.mIsV3) {
            a.setContentView(R.layout.activity_main_v3);
            a.mUiController = new V3LayoutController(a);
        } else {
            a.setContentView(R.layout.activity_main);
            a.mUiController = new MainLayoutController(a);
            a.applyLayout2SidePreference();
        }
        a.mUiMediator.bindViews();
    }

    private static void bindSignalMeter(MainActivity a) {
        a.mSignalBarsView = a.findViewById(R.id.viewSignalBars);
        a.mSignalMeterCoordinator = new SignalMeterCoordinator(a);
        a.mSignalMeterCoordinator.bind(a.mUiMediator.ivSignalLevel, a.mSignalBarsView);
        a.mSignalMeterCoordinator.applyModeVisibility();
    }

    private static void ensureFirstRunLanguageCountry(MainActivity a) {
        // Primer inicio tras instalación: solicitar idioma y país.
        // Explicación: mejora la selección de logos y streaming (filtrado por country_code en Supabase).
        a.ensureFirstRunLanguageAndCountry();
    }

    private static void initUiControllerViewsFontsIcons(MainActivity a) {
        // V21.0: Initialize the active UI Controller
        if (a.mUiController != null) {
            a.mUiController.initViews(a.findViewById(android.R.id.content));
            a.mUiMediator.bindViews();
        }

        // Arranque fluido: evitar trabajo recursivo pesado antes del primer draw.
        // (En algunos headunits causa "Skipped XX frames" al iniciar tras reboot/instalación).
        View root = a.findViewById(android.R.id.content);
        if (root != null) {
            root.post(() -> {
                try { a.applyFonts(); } catch (Exception ignored) {}
                try { a.applyIconPack(); } catch (Exception ignored) {}
            });
        } else {
            // Fallback (no debería ocurrir)
            a.applyFonts();
            a.applyIconPack();
        }

        // VXX: Aplicar relieve opcional de logos
        if (a.mPrefs != null) {
            a.applyReliefHd(a.mPrefs.getBoolean("pref_relief_hd", false));
        }

        // V3.8: Premium Background Binding
    }

    private static void maybeRequestStoragePermission(MainActivity a) {
        if (a.checkSelfPermission(
                android.Manifest.permission.READ_EXTERNAL_STORAGE) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            a.requestPermissions(new String[] { android.Manifest.permission.READ_EXTERNAL_STORAGE }, 100);
        }
    }

    private static void initLogoServiceAndPlaybackStack(MainActivity a) {
        // V13: Inicializar Managers agnósticos
        a.mLogoManager = new LogoManager(a);
        if (a.mRepository != null) {
            a.mSupabaseSyncManager = new com.example.openradiofm.data.source.SupabaseSyncManager(a, a.mRepository.getSupabaseSource());
        }
        a.mServiceController = new RadioServiceController(a, a.mPrefs, a.mServiceListener);

        // V16: NightMode y History Managers
        a.mNightModeManager = new NightModeManager(a, a.mPrefs, freq -> {
            // V18.6.4: Pasar el nombre RDS actual para no perderlo al cambiar de skin
            String currentName = (a.mRdsManager != null) ? a.mRdsManager.getDisplayName(freq) : a.mLastPs;
            a.updateFrequencyDisplay(freq, currentName);
        });
        a.mDayModeManager = new DayModeManager(a, a.mPrefs, freq -> {
            String currentName = (a.mRdsManager != null) ? a.mRdsManager.getDisplayName(freq) : a.mLastPs;
            a.updateFrequencyDisplay(freq, currentName);
        });
        a.mHistoryManager = new HistoryManager(a, a.mPrefs);
        a.mMediaSessionManager = new MediaSessionManager(a);
        // Arranque fluido: la conexión al MediaBrowser/MediaSession puede provocar jank
        // en algunas ROMs justo antes del primer draw. Diferir un tick.
        try {
            a.getMainHandler().post(() -> {
                try { if (a.mMediaSessionManager != null) a.mMediaSessionManager.connect(); } catch (Exception ignored) {}
            });
        } catch (Exception ignored) {
            a.mMediaSessionManager.connect();
        }

        a.mControlPanelManager = new ControlPanelManager(a);

        // V5.5: Inicializar PlaybackManager y DeviceManager
        a.mPlaybackManager = new PlaybackManager(a);
        a.mPlaybackManager.init(a.mEngine, new PlaybackManager.PlaybackListener() {
            @Override
            public void onMuteStateChanged(boolean isMuted) {
                a.mMuteState = isMuted;
                a.runOnUiThread(() -> {
                    if (a.mUiController != null) {
                        a.mUiController.updateMute(isMuted);
                    }
                    if (a.mUiMediator.btnMute != null) {
                        a.mUiMediator.btnMute.setSelected(isMuted);
                        // boolean isMTK = a.mEngine != null && a.mEngine.getEngineName().contains("MTK"); // Removed as per instruction

                        if (isMuted) {
                            MainActivity.setImageResourceIfChanged(a.mUiMediator.btnMute, R.drawable.radio_mute_p);
                        } else {
                            MainActivity.setImageResourceIfChanged(a.mUiMediator.btnMute, R.drawable.radio_mute_n);
                        }
                        // Reaplicar pack si existe (evita volver a default al cambiar estado)
                        if (a.mIconPackManager != null) {
                            a.mIconPackManager.apply(a.mUiMediator.btnMute, isMuted ? "radio_mute_p" : "radio_mute_n",
                                    isMuted ? R.drawable.radio_mute_p : R.drawable.radio_mute_n);
                        }
                        // V2.5: Preservar tinte noche si activo
                        Object savedFilter = a.mUiMediator.btnMute.getTag(R.id.tag_color_filter);
                        if (savedFilter instanceof Integer) {
                            a.mUiMediator.btnMute.setColorFilter((Integer) savedFilter, android.graphics.PorterDuff.Mode.SRC_IN);
                        }
                        a.mUiMediator.btnMute.setAlpha(1.0f);
                        if (!isMuted) a.mUiMediator.btnMute.setSelected(false);
                    }
                });
            }

            @Override
            public void onMediaCommand(String command) {
                a.runOnUiThread(() -> {
                    boolean usePresetMode = a.mPrefs != null
                            && a.mPrefs.getInt("pref_steering_next_prev_mode", 0) == 1;
                    switch (command) {
                        case "ACTION_NEXT":
                            if (usePresetMode) {
                                if (a.mPresetManager != null) a.mPresetManager.playNextPreset();
                            } else if (a.mEngine != null) {
                                a.mEngine.seekUp();
                            }
                            break;
                        case "ACTION_PREV":
                            if (usePresetMode) {
                                if (a.mPresetManager != null) a.mPresetManager.playPrevPreset();
                            } else if (a.mEngine != null) {
                                a.mEngine.seekDown();
                            }
                            break;
                    }
                });
            }
        });
        // Evitar registro pesado en el tramo crítico del primer render.
        try {
            a.getMainHandler().post(() -> {
                try { if (a.mPlaybackManager != null) a.mPlaybackManager.registerMediaReceiver(); } catch (Exception ignored) {}
            });
        } catch (Exception ignored) {
            a.mPlaybackManager.registerMediaReceiver();
        }

        a.mDeviceManager = new DeviceManager(a);
    }

    private static void loadLogoAssetsAndSimpleLayoutManager(MainActivity a) {
        // V2.0: Cargar fondo personalizado si existe
        a.mLogoManager.loadCustomBackground();
        a.mLogoManager.loadCarLogo();

        a.mSimpleLayoutManager = new SimpleLayoutManager(a);
    }

    private static void bootstrapLastFrequencyAndBands(MainActivity a) {
        // V13: Cargar última frecuencia guardada
        if (a.mLastFreq == -1) {
            a.mLastFreq = a.mPrefs.getInt("pref_last_freq", 87500);
        }
        a.mLastBand = a.mPrefs.getInt("pref_last_band", MainActivity.BAND_FM1);
        // Saneo 1 sola vez: si QS6 viene con pref contaminada en bootstrap (87.5/87.6),
        // intentamos recuperar una frecuencia más fiable desde el motor antes de persistir otra vez.
        if (a.mMode == MainActivity.FmMode.FM_QS6
                && !a.mPrefs.getBoolean(MainActivity.PREF_QS6_BOOTSTRAP_SANITIZED, false)
                && (a.mLastFreq == 87500 || a.mLastFreq == 87600)
                && a.mEngine != null) {
            try {
                int engineFreq = a.mEngine.getCurrentFreq();
                int engineBand = a.mEngine.getCurrentBand();
                if (engineFreq > 0 && engineFreq != 87500 && engineFreq != 87600) {
                    a.mLastFreq = engineFreq;
                    a.mLastBand = engineBand;
                    a.mPrefs.edit()
                            .putInt("pref_last_freq", a.mLastFreq)
                            .putInt("pref_last_band", a.mLastBand)
                            .putBoolean(MainActivity.PREF_QS6_BOOTSTRAP_SANITIZED, true)
                            .apply();
                    Log.d(MainActivity.TAG, "QS6 sanitize: bootstrap pref replaced with engine freq "
                            + a.mLastFreq + "/B" + a.mLastBand);
                } else {
                    a.mPrefs.edit().putBoolean(MainActivity.PREF_QS6_BOOTSTRAP_SANITIZED, true).apply();
                    Log.d(MainActivity.TAG, "QS6 sanitize: bootstrap pref kept (no reliable engine freq yet)");
                }
            } catch (Exception e) {
                a.mPrefs.edit().putBoolean(MainActivity.PREF_QS6_BOOTSTRAP_SANITIZED, true).apply();
                Log.w(MainActivity.TAG, "QS6 sanitize check failed", e);
            }
        }
        if (a.mMode == MainActivity.FmMode.FM_K706
                && !a.mPrefs.getBoolean(MainActivity.PREF_K706_BOOTSTRAP_SANITIZED, false)
                && (a.mLastFreq == 87500 || a.mLastFreq == 87600)
                && a.mEngine != null) {
            try {
                int engineFreq = a.mEngine.getCurrentFreq();
                int engineBand = a.mEngine.getCurrentBand();
                if (engineFreq > 0 && engineFreq != 87500 && engineFreq != 87600) {
                    a.mLastFreq = engineFreq;
                    a.mLastBand = engineBand;
                    a.mPrefs.edit()
                            .putInt("pref_last_freq", a.mLastFreq)
                            .putInt("pref_last_band", a.mLastBand)
                            .putBoolean(MainActivity.PREF_K706_BOOTSTRAP_SANITIZED, true)
                            .apply();
                    Log.d(MainActivity.TAG, "K706 sanitize: bootstrap pref replaced with engine freq "
                            + a.mLastFreq + "/B" + a.mLastBand);
                } else {
                    a.mPrefs.edit().putBoolean(MainActivity.PREF_K706_BOOTSTRAP_SANITIZED, true).apply();
                    Log.d(MainActivity.TAG, "K706 sanitize: bootstrap pref kept (no reliable engine freq yet)");
                }
            } catch (Exception e) {
                a.mPrefs.edit().putBoolean(MainActivity.PREF_K706_BOOTSTRAP_SANITIZED, true).apply();
                Log.w(MainActivity.TAG, "K706 sanitize check failed", e);
            }
        }
        a.mStartupFqGuards.startupSavedFreqKhz = a.mLastFreq;

        // V22.4: Saneo de arranque para prevenir bucles de Startup Reinforce
        // 1. Corregir escala de frecuencia (unidades NWD 10kHz vs app kHz)
        if (a.mMode == MainActivity.FmMode.FM_QS6 && a.mLastFreq > 0 && a.mLastFreq < 20000) {
            Log.w(MainActivity.TAG, "Startup: Detectada frecuencia NWD (10kHz units: " + a.mLastFreq + "). Escalando a kHz.");
            a.mLastFreq *= 10;
        }

        // 2. Corregir banda incoherente (FM no puede ser banda >= 3)
        if (a.mLastFreq > 30000 && a.mLastBand >= 3) {
            Log.w(MainActivity.TAG, "Startup: Detectada banda AM (" + a.mLastBand + ") para frecuencia FM (" + a.mLastFreq + "). Forzando FM1.");
            a.mLastBand = MainActivity.BAND_FM1;
            a.mCurrentBand = a.mLastBand;
            a.mPrefs.edit().putInt("pref_last_band", a.mLastBand).apply();
        } else if (a.mLastFreq <= 30000 && a.mLastBand < 3 && a.mLastFreq > 0) {
            Log.w(MainActivity.TAG, "Startup: Detectada banda FM (" + a.mLastBand + ") para frecuencia AM (" + a.mLastFreq + "). Forzando AM1.");
            a.mLastBand = MainActivity.BAND_AM1;
            a.mCurrentBand = a.mLastBand;
            a.mPrefs.edit().putInt("pref_last_band", a.mLastBand).apply();
        } else {
            a.mCurrentBand = a.mLastBand;
        }

        a.mStartupFqGuards.startupPersistGuardUntilMs = android.os.SystemClock.elapsedRealtime() + 6000L;
        a.mStartupFqGuards.startupRetuneAttempts = 0;
    }

    private static void initSimpleLayoutIfNeeded(MainActivity a) {
        if (a.mIsSimpleLayout) {
            a.mSimpleLayoutManager.initViews(a.findViewById(android.R.id.content));
        }
    }

    private static void bindMainFrequencyViewsAndClock(MainActivity a) {
        // Bind Views
        a.tvFrequency = a.findViewById(R.id.tvFrequency);
        if (a.tvFrequency != null) {
            a.tvFrequency.setEllipsize(null);
            a.tvFrequency.setSingleLine(false); // Necesario para que el Autosizing no se confunda con ellipsize
            a.tvFrequency.setMaxLines(1);
            // V7.2f: El listener real se configura en a.setupCreditsEasterEgg() para evitar redundancias
            // y mantener la funcionalidad de historial + créditos.
        }
        a.tvRdsName = a.findViewById(R.id.tvRdsName); // V5
        a.tvRdsInfo = a.findViewById(R.id.tvRdsInfo);

        // V4.3: New UI Elements
        a.tvPty = a.findViewById(R.id.tvPty);

        a.ivBandIndicator = a.findViewById(R.id.ivBandIndicator);
        a.ivUnitLabel = a.findViewById(R.id.ivUnitLabel);

        // V18.5: Inicializar Reloj Digital
        a.mClockHandler = new android.os.Handler(android.os.Looper.getMainLooper());
        a.mClockRunnable = new Runnable() {
            @Override
            public void run() {
                if (a.mUiMediator.tvDigitalClock != null && a.mUiMediator.tvDigitalClock.getVisibility() == View.VISIBLE) {
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault());
                    a.mUiMediator.tvDigitalClock.setText(sdf.format(new java.util.Date()));
                }
                a.mClockHandler.postDelayed(this, 10000); // 10 segs (suficiente para HH:mm)
            }
        };
    }

    private static void applyLogoModeBindIndicatorsAndStreaming(MainActivity a) {
        // Aplicar preferencias iniciales
        a.applyLogoModePreference();
        a.ivFavoriteIndicator = a.findViewById(R.id.ivFavoriteIndicator);
        a.ivStereoIcon = a.findViewById(R.id.ivStereoIcon);
        a.ivAfIcon = a.findViewById(R.id.ivAfIcon);
        a.ivTaIcon = a.findViewById(R.id.ivTaIcon);
        a.ivTpIcon = a.findViewById(R.id.ivTpIcon);
        a.ivDataActivity = a.findViewById(R.id.ivDataActivity);

        a.setupOnlineStreaming();

        // El listener de a.mRepository se configura asincronamente en onModeDetected
    }

    private static void wireRdsIconsAndStereoToggle(MainActivity a) {
        // V9.9: RDS Icons must be dimmed by default, not gone.
        // V5.0: RDS Icons - Ahora usan a.mEngine (sin bifurcación por modo)
        if (a.ivAfIcon != null) {
            a.ivAfIcon.setAlpha(0.2f);
            a.ivAfIcon.setOnClickListener(v -> {
                a.animateButton(a.ivAfIcon);
                if (a.mEngine != null)
                    a.mEngine.toggleRdsFeature(1); // AF
            });
        }
        if (a.ivTaIcon != null) {
            a.ivTaIcon.setAlpha(0.2f);
            a.ivTaIcon.setOnClickListener(v -> {
                a.animateButton(a.ivTaIcon);
                if (a.mEngine != null)
                    a.mEngine.toggleRdsFeature(2); // TA
            });
        }
        if (a.ivTpIcon != null) {
            a.ivTpIcon.setAlpha(0.2f);
            a.ivTpIcon.setOnClickListener(v -> {
                a.animateButton(a.ivTpIcon);
                if (a.mEngine != null)
                    a.mEngine.toggleRdsFeature(0); // RDS global
            });
        }

        // V7.2f: Botón ST dinámico con dos estados (Stereo/Mono)
        if (a.ivStereoIcon != null) {
            a.ivStereoIcon.setVisibility(View.VISIBLE); // Siempre visible
            a.refreshStereoIndicatorUi(null);

            a.ivStereoIcon.setOnClickListener(v -> {
                a.animateButton(a.ivStereoIcon);
                boolean current = a.mPrefs.getBoolean("pref_stereo_mode_on", true);
                boolean next = !current;

                // Guardar y Aplicar
                a.mPrefs.edit().putBoolean("pref_stereo_mode_on", next).apply();
                if (a.mEngine != null) {
                    a.mEngine.setStereo(next);
                }
                a.refreshStereoIndicatorUi(null);
                a.showToast(next ? "Modo Stereo Activado" : "Modo Forzar Mono");
            });
        }
    }

    private static void wireCarLogoAndControlPanel(MainActivity a) {
        // V16.2: Skin cycling remains in Car Logo (as it's more visual)
        if (a.mUiMediator.ivCarLogo != null) {
            a.mUiMediator.ivCarLogo.setOnClickListener(v -> {
                com.example.openradiofm.ui.theme.ThemeManager.Skin next = a.mThemeManager.cycleSkin();
                a.applySkin(next);
                a.showToast(a.getString(R.string.toast_skin_colon, next.displayName));
            });
            a.mUiMediator.ivCarLogo.setOnLongClickListener(v -> {
                if (a.mDialogManager != null) a.mDialogManager.showHistoryDialog();
                return true;
            });
        }

        // Indicators Binding - REMOVED

        // Configurar controles (Delegados a ControlPanelManager)
        if (a.mControlPanelManager != null) a.mControlPanelManager.initViews();

        // Configurar indicadores de estado (Eliminados)
        // setupIndicators();

        // V16.2: Inicializar ThemeManager
    }

    private static void initThemeSkinNightAndAutoHideV3(MainActivity a) {
        a.mThemeManager = new com.example.openradiofm.ui.theme.ThemeManager(a);
        a.mThemeManager.setLayoutPrefs(a.mPrefs);
        // V2.5: Eliminado SkinAppliedListener redundante. a.applySkin() ahora gestiona todo secuencialmente.
        a.applySkin(a.mThemeManager.getCurrentSkin());
        a.checkAndApplyNightMode(); // V4: Automatic Night Mode

        // V18.6: Auto-hide bottom controls initialization

        a.mAutoHideHandler = new android.os.Handler(android.os.Looper.getMainLooper());
        a.mAutoHideRunnable = () -> a.hideBottomControls();

        // En Layout V3, interceptar toques en el fondo para mostrar controles
        if (a.isV3LayoutActive()) {
            android.view.View root = a.findViewById(R.id.rootLayout);
            if (root != null) {
                root.setOnTouchListener((v, event) -> {
                    if (event.getAction() == android.view.MotionEvent.ACTION_DOWN) {
                        if (a.mControlsHidden) {
                            a.showBottomControls();
                        } else {
                            a.resetAutoHideTimer();
                        }
                    }
                    return false; // Permitir que otros elementos reciban el toque
                });
            }
            a.resetAutoHideTimer();
        }

        // Seeking Logic (Delegated to ControlPanelManager)
    }

    private static void finalizeBootstrap(MainActivity a) {
        // Importante: arrancar el ServiceController inmediatamente. En algunas ROMs/headunits,
        // los callbacks posteados pueden no ejecutarse de forma fiable en el primer frame (UI "parece viva"
        // pero los controles no surten efecto porque el stack de servicio no arranca).
        try {
            if (a.mServiceController != null) {
                a.mServiceController.start();
            }
        } catch (Throwable t) {
            Log.w(MainActivity.TAG, "ServiceController.start() falló", t);
        }

        // Fase final: diferir operaciones no críticas al siguiente ciclo de UI para no penalizar
        // el primer render (evita "Skipped XX frames" al arranque).
        View root = a.findViewById(android.R.id.content);
        Runnable late = () -> {
            try { a.applyFonts(); } catch (Exception ignored) {}
            try { a.setupCreditsEasterEgg(); } catch (Exception ignored) {}
            try { a.scheduleRadioUiResyncAfterRecreation(); } catch (Exception ignored) {}
            try { a.adjustLayoutForDPI(); } catch (Exception ignored) {}
        };
        if (root != null) {
            root.post(late);
        } else {
            late.run();
        }
    }
}
