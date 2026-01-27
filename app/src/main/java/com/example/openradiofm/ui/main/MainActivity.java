package com.example.openradiofm.ui.main;

import androidx.appcompat.app.AppCompatActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Color;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

import com.hcn.autoradio.IRadioServiceAPI;
import com.hcn.autoradio.IRadioCallBack;
import com.example.openradiofm.data.source.HiddenRadioPlayer;
import com.example.openradiofm.R;

/**
 * Pantalla principal de la radio FM.
 *
 * Responsabilidades:
 * - Conectarse al servicio de radio del coche (IRadioServiceAPI).
 * - Mostrar frecuencia, nombre RDS y texto RDS.
 * - Gestionar presets, logos locales y botones de control.
 *
 * Notas de diseño:
 * - El sondeo del estado de la radio se hace en un hilo de fondo mediante Timer,
 *   y solo las actualizaciones de UI pasan por runOnUiThread() para no bloquear
 *   el hilo principal.
 * - Los recursos de hardware (servicio, proceso root, listener RDS oculto) se
 *   liberan explícitamente en onDestroy() para evitar fugas de memoria.
 */
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "OpenRadioFm";

    /**
     * Modos de funcionamiento de la app:
     * - FM_COMPLETO: dispositivo con root + servicio especial del coche disponible.
     * - FM_BASICO: sin root o sin servicio; solo frecuencia + logos en SD (y, más adelante, nombres manuales).
     */
    private enum FmMode {
        FM_COMPLETO,
        FM_BASICO
    }

    private IRadioServiceAPI mRadioService;
    private HiddenRadioPlayer mHiddenPlayer;
    private int mLastFreq = 0;
    private String mLastLogoUrl = ""; // V8.4: Caching Logic
    
    // Modo de funcionamiento actual (se decide en el arranque).
    private FmMode mMode = FmMode.FM_BASICO;

    // Repositorio
    private com.example.openradiofm.data.repository.RadioRepository mRepository;

    // UI Views V4
    private TextView tvFrequency;
    private TextView tvRdsName; // V5 New
    private TextView tvRdsInfo;
    private ImageButton btnLocDx; 
    private ImageButton btnBand;  
    
    // Smart Cards
    private ImageView[] ivPresets = new ImageView[6];
    private TextView[] tvPresets = new TextView[6];
    private View[] cardPresets = new View[6]; // Changed from Button[] to View[]

    // Settings Indicators (TextViews Removed)


    private android.content.SharedPreferences mPrefs;
    private int mCurrentBand = 0; // Cache for band-specific presets
    
    private final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mRadioService = IRadioServiceAPI.Stub.asInterface(service);
            try {
                mRadioService.registerRadioCallback(mCallback);
                startStatusPolling();
                showToast("Conexión Establecida");
                // Solo inicializamos el listener oculto de RDS en modo completo.
                if (mMode == FmMode.FM_COMPLETO) {
                    initHiddenPlayer();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            stopStatusPolling();
            mRadioService = null;
        }
    };

    private void initHiddenPlayer() {
        mHiddenPlayer = new HiddenRadioPlayer(new HiddenRadioPlayer.Listener() {
            @Override
            public void onRdsText(String text) {
                runOnUiThread(() -> {
                    if (tvRdsInfo != null) {
                        String current = tvRdsInfo.getText().toString();
                        if (!current.equals(text)) {
                            tvRdsInfo.setText(text);
                        }
                    }
                });
            }
            @Override
            public void onRdsName(String name) {
                // UI update via polling
            }
            @Override
            public void onRawEvent(int code, Object info, String str) {}
        });
        if (!mHiddenPlayer.init()) Log.e(TAG, "Error RDS Hardware Init");
    }

    // ScheduledExecutorService para sondear el estado de la radio en segundo plano.
    // Más robusto que Timer y evita fugas de memoria.
    private java.util.concurrent.ScheduledExecutorService mPollingExecutor;
    /**
     * Inicia el sondeo periódico del estado de la radio.
     *
     * IMPORTANTE:
     * - El trabajo pesado (llamadas AIDL y acceso al repositorio/root) se ejecuta
     *   en el hilo del ScheduledExecutorService (en segundo plano).
     * - Solo el pintado de la interfaz se hace dentro de runOnUiThread().
     */
    private void startStatusPolling() {
        stopStatusPolling();
        mPollingExecutor = java.util.concurrent.Executors.newSingleThreadScheduledExecutor();
        mPollingExecutor.scheduleAtFixedRate(() -> {
            // Ejecutamos la lógica de refresco directamente en el hilo del executor.
            // Dentro de refreshRadioStatus() se usa runOnUiThread() solo para la UI.
            refreshRadioStatus();
        }, 500, 500, java.util.concurrent.TimeUnit.MILLISECONDS);
    }
    /**
     * Detiene el sondeo de estado si está activo.
     */
    private void stopStatusPolling() {
        if (mPollingExecutor != null) {
            mPollingExecutor.shutdownNow();
            mPollingExecutor = null;
        }
    }

    // Callback mínimo para eventos del servicio de radio. De momento no se usa,
    // pero es importante desregistrarlo en onDestroy() para no filtrar la Activity.
    private final IRadioCallBack mCallback = new IRadioCallBack.Stub() {
        @Override public void onEvent(int code, String data) {}
    };

    private int mTestClickCount = 0;
    private long mTestStartTime = 0;

    /**
     * Envía una tecla al MCU del coche usando la API interna android.carsource.McuManager.
     * Todo el acceso va envuelto en try/catch para que en dispositivos sin esta clase
     * simplemente se muestre un Toast y no se cierre la app.
     */
    private void sendMcuKey(int key) {
        try {
            Class<?> mcuClass = Class.forName("android.carsource.McuManager");
            java.lang.reflect.Method getInstance = mcuClass.getMethod("getsInstance");
            Object instance = getInstance.invoke(null);
            java.lang.reflect.Method injectKey = mcuClass.getMethod("injectKeyEventTimeout", int.class, int.class);
            injectKey.invoke(instance, key, 0x32);
            Log.d(TAG, "MCU Key injected: " + key);
        } catch (Exception e) {
            Log.e(TAG, "Error injecting MCU key: " + e.getMessage());
            showToast("Hardware EQ not supported on this device");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_OpenRadioFm);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] { android.Manifest.permission.READ_EXTERNAL_STORAGE }, 100);
        }

        // Determinar modo de funcionamiento (FM completo vs básico) antes de crear el repositorio.
        mMode = detectMode();
        Log.d(TAG, "Modo de funcionamiento: " + mMode);

        if (mMode == FmMode.FM_BASICO) {
            showToast("Modo Básico: Sin Root (Nombres Manuales)");
        } else {
            showToast("Modo Completo: Root + Servicio HCN");
        }

        // Repositorio de datos (nombres RDS por root + logos locales/cloud).
        // En MODO_FM_BASICO desactivamos por completo el uso de root.
        mRepository = new com.example.openradiofm.data.repository.RadioRepository(this, mMode == FmMode.FM_COMPLETO);
        // Preferencias para presets y estados de indicadores (TA/AF/TP, etc.).
        mPrefs = getSharedPreferences("RadioPresets", MODE_PRIVATE);

        // Bind Views
        tvFrequency = findViewById(R.id.tvFrequency);
        tvRdsName = findViewById(R.id.tvRdsName); // V5
        tvRdsInfo = findViewById(R.id.tvRdsInfo);
        
        btnLocDx = findViewById(R.id.btnLocDx);
        btnBand = findViewById(R.id.btnBand);
        
        
        // Indicators Binding - REMOVED


        
        // Configurar controles (EQ, Mute, Test, AutoScan, LOC/DX)
        setupControlButtons();
        
        // Configurar indicadores de estado (Eliminados)
        // setupIndicators();
        
        // Aplicar Skin guardado
        com.example.openradiofm.ui.theme.ThemeManager themeManager = new com.example.openradiofm.ui.theme.ThemeManager(this);
        applySkin(themeManager.getCurrentSkin());
        
        // Seeking Logic
        setupSeekButtons();

        // Presets Binding
        bindPresetViews();
        // Initial Refresh (will be updated again when band is fetched)
        refreshPresetButtons();
        
        setupRdsText();
        applyFonts();
        
        // V10: Custom User Names
        setupCustomNameEditing();

        // Conectamos con el servicio de radio del coche.
        conectarRadio();
    }
    
    /**
     * Configura los botones de control (EQ, Mute, Test, AutoScan, LOC/DX).
     */
    private void setupControlButtons() {
        // EQ Logic (V8: MCU Injection)
        ImageButton btnEq = findViewById(R.id.btnSettings);
        btnEq.setOnClickListener(v -> sendMcuKey(0x134)); // Keycode 308 for DSP
        
        // Long Click para abrir selector de skins
        btnEq.setOnLongClickListener(v -> {
            showSkinSelectorDialog();
            return true;
        });

        // Mute Logic (System Audio)
        ImageButton btnMute = findViewById(R.id.btnMute);
        android.media.AudioManager am = (android.media.AudioManager) getSystemService(Context.AUDIO_SERVICE);
        btnMute.setOnClickListener(v -> {
            boolean isSelected = !v.isSelected();
            v.setSelected(isSelected);
            if (isSelected) {
                am.adjustStreamVolume(android.media.AudioManager.STREAM_MUSIC, android.media.AudioManager.ADJUST_MUTE, 0);
                btnMute.setImageResource(R.drawable.radio_mute_p); // Active
            } else {
                am.adjustStreamVolume(android.media.AudioManager.STREAM_MUSIC, android.media.AudioManager.ADJUST_UNMUTE, 0);
                btnMute.setImageResource(R.drawable.radio_mute_n); // Inactive
            }
        });

        // Test/Hidden Menu (V8: 5 Clicks logic)
        findViewById(R.id.btnTest).setOnClickListener(v -> {
            long now = System.currentTimeMillis();
            if (mTestClickCount == 0 || (now - mTestStartTime) > 15000) {
                mTestClickCount = 1;
                mTestStartTime = now;
            } else {
                mTestClickCount++;
            }

            if (mTestClickCount >= 5) {
                mTestClickCount = 0; // Reset
                Intent intent = new Intent();
                intent.setComponent(new android.content.ComponentName("com.hcn.changedapp", "com.hcn.changedapp.TestActivity"));
                try {
                    startActivity(intent);
                } catch (Exception e) {
                    showToast("Error al abrir TestActivity");
                }
            } else {
                showToast("Faltan " + (5 - mTestClickCount) + " clics");
            }
        });

        // Auto Scan
        findViewById(R.id.btnAutoScan).setOnClickListener(v -> execRemote(IRadioServiceAPI::onScanEvent));
        
        // LOC/DX Switch
        btnLocDx.setOnClickListener(v -> execRemote(IRadioServiceAPI::onLocDxEvent));
    }
    
    // setupIndicators removed


    private void setupCustomNameEditing() {
        // Permitir editar el nombre al mantener pulsado el texto del nombre RDS
        tvRdsName.setOnLongClickListener(v -> {
            showEditNameDialog();
            return true;
        });

        // También en el logo principal, por si tvRdsName está vacío
        findViewById(R.id.ivMainLogo).setOnLongClickListener(v -> {
            showEditNameDialog();
            return true;
        });
    }

    private void showEditNameDialog() {
        if (mRadioService == null) return;
        try {
            int currentFreq = mRadioService.getCurrentFreq();
            
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
            builder.setTitle("Editar nombre de emisora");
            builder.setMessage("Frecuencia: " + String.format("%.1f MHz", currentFreq / 1000.0));

            final android.widget.EditText input = new android.widget.EditText(this);
            input.setSingleLine(true);
            
            // Pre-llenar con el nombre actual (si es custom o RDS)
            com.example.openradiofm.data.model.RadioStation s = mRepository.getStationInfo(currentFreq, null);
            if (s.getName() != null) {
                input.setText(s.getName());
                input.setSelectAllOnFocus(true);
            }
            
            builder.setView(input);

            builder.setPositiveButton("Guardar", (dialog, which) -> {
                String newName = input.getText().toString();
                // Guardar en repositorio (SharedPreferences)
                mRepository.setCustomName(currentFreq, newName);
                showToast("Nombre guardado");
                // Forzar refresco inmediato de UI
                refreshRadioStatus();
            });

            builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());
            
            // Botón para borrar nombre personalizado y volver al original/RDS
            builder.setNeutralButton("Restaurar Original", (dialog, which) -> {
                mRepository.setCustomName(currentFreq, null); // Null borra la entrada custom
                showToast("Nombre restaurado");
                refreshRadioStatus();
            });

            builder.show();
            input.requestFocus(); // Focus automático
            
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // --------------------------------------------------------------------------------
        // LIMPIEZA DE RECURSOS (CRÍTICO PARA EVITAR FUGAS DE MEMORIA)
        // --------------------------------------------------------------------------------

        // 1) Detener el Timer de sondeo.
        // Si no lo paramos, el hilo del Timer seguirá ejecutándose en fondo intentando
        // acceder a esta Activity destruida, causando crashes o fugas.
        stopStatusPolling();

        // 2) Desconectar del Servicio de Radio del Coche.
        // Es fundamental desregistrar el callback y hacer unbind para que Android sepa
        // que ya no necesitamos el servicio y pueda liberar recursos del sistema.
        try {
            if (mRadioService != null) {
                mRadioService.unRegisterRadioCallback(mCallback);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            unbindService(mConnection);
        } catch (Exception e) {
            // Si ya estaba desregistrado, ignoramos.
        }

        // 3) Liberar el listener de la API oculta de radio.
        // HiddenRadioPlayer mantiene una referencia a nuestros callbacks. Al liberarlo,
        // rompemos esa referencia circular.
        if (mHiddenPlayer != null) {
            mHiddenPlayer.release();
            mHiddenPlayer = null;
        }

        // 4) Cerrar procesos Root (Shell).
        // RootRDSSource abre un proceso "su" persistente. Si no lo cerramos con "exit",
        // ese proceso se quedaría huérfano en el sistema consumiendo RAM y CPU.
        if (mRepository != null) {
            mRepository.shutdown();
            mRepository = null;
        }
    }

    /**
     * Detecta el modo de funcionamiento de la app.
     * 
     * MODO_FM_COMPLETO:
     * - Se activa solo si tenemos ROOT ("su") y el SERVICIO HCN ("com.hcn.autoradio").
     * - Permite leer nombres RDS avanzados directamente de archivos del sistema.
     * 
     * MODO_FM_BASICO:
     * - Se usa en dispositivos normales o si falta el servicio del coche.
     * - Desactiva totalmente el código Root para evitar permisos denegados o crashes.
     */
    private FmMode detectMode() {
        boolean hasService = hasCarRadioService();
        boolean hasRootBinary = hasRootBinary();

        if (hasService && hasRootBinary) {
            return FmMode.FM_COMPLETO;
        }
        return FmMode.FM_BASICO;
    }

    /**
     * Comprueba si el sistema anuncia el servicio de radio del coche
     * con la action com.hcn.autoradio.FM_PLUG_SERVICE.
     */
    private boolean hasCarRadioService() {
        try {
            android.content.pm.PackageManager pm = getPackageManager();
            Intent intent = new Intent("com.hcn.autoradio.FM_PLUG_SERVICE");
            intent.setPackage("com.hcn.autoradio");
            java.util.List<android.content.pm.ResolveInfo> list =
                    pm.queryIntentServices(intent, 0);
            return list != null && !list.isEmpty();
        } catch (Exception e) {
            Log.e(TAG, "Error comprobando servicio de radio del coche", e);
            return false;
        }
    }

    /**
     * Comprobación simple de root: verifica si existe un binario su
     * en rutas típicas del sistema. No ejecuta su, por lo que es segura.
     */
    private boolean hasRootBinary() {
        String[] paths = {
                "/system/xbin/su",
                "/system/bin/su",
                "/system/su",
                "/sbin/su"
        };
        for (String path : paths) {
            try {
                java.io.File f = new java.io.File(path);
                if (f.exists()) {
                    return true;
                }
            } catch (Exception ignored) {
            }
        }
        return false;
    }

    private void applyFonts() {
        try {
            android.graphics.Typeface orbitron = android.graphics.Typeface.createFromAsset(getAssets(), "fonts/orbitron_bold.ttf");
            tvFrequency.setTypeface(orbitron);
            // V8.3: Apply to all main text views
            tvRdsName.setTypeface(orbitron); 
            tvRdsInfo.setTypeface(orbitron); 
            
            // Apply to preset numbers too
            for (TextView tv : tvPresets) {
                if (tv != null) tv.setTypeface(orbitron);
            }
        } catch (Exception e) {
            Log.w(TAG, "Orbitron font not found in assets/fonts. Skipping.");
        }
    }
    
    private void setupRdsText() {
         // V8.3: Enable Marquee on RDS Info
         tvRdsInfo.setText(""); // Start Empty
         tvRdsInfo.setSelected(true); // Required for Marquee
         tvRdsInfo.setSingleLine(true);
         tvRdsInfo.setEllipsize(android.text.TextUtils.TruncateAt.MARQUEE);
    }

    /**
     * Configura los botones de búsqueda manual y automática de frecuencias.
     *
     * - Los pasos manuales (+/- 0.1 MHz) se hacen mediante llamadas AIDL al servicio,
     *   que se ejecutan fuera del hilo principal cuando es posible.
     * - Las búsquedas largas (seek up/down) usan los eventos específicos del servicio.
     */
    private void setupSeekButtons() {
        ImageButton btnSeekUp = findViewById(R.id.btnSeekUp);     // VISUALLY LEFT (<) -> DOWN (Decrement)
        ImageButton btnSeekDown = findViewById(R.id.btnSeekDown); // VISUALLY RIGHT (>) -> UP (Increment)
        
        // V8.4: Custom Seek Step (0.1 MHz)
        // We use gotoFreq(current +/- 100) instead of onManualUp/Down because default step is 0.05
        
        btnSeekUp.setOnClickListener(v -> {
             if (mRadioService == null) return;
             try {
                 int current = mRadioService.getCurrentFreq();
                 int newFreq = current - 50; // -0.05 MHz (Was 0.1)
                 if (newFreq < 87500) newFreq = 108000; // Wrap Around
                 mRadioService.gotoFreq(newFreq);
             } catch (RemoteException e) { e.printStackTrace(); }
             // Old: execRemote(IRadioServiceAPI::onManualDownEvent);
        });
        
        btnSeekUp.setOnLongClickListener(v -> {
            execRemote(IRadioServiceAPI::onSeekUpEvent); // V9: Swapped
            return true;
        });

        // Right Button (>) -> Increment
        btnSeekDown.setOnClickListener(v -> {
             if (mRadioService == null) return;
             try {
                 int current = mRadioService.getCurrentFreq();
                 int newFreq = current + 50; // +0.05 MHz (Was 0.1)
                 if (newFreq > 108000) newFreq = 87500; // Wrap Around
                 mRadioService.gotoFreq(newFreq);
             } catch (RemoteException e) { e.printStackTrace(); }
             // Old: execRemote(IRadioServiceAPI::onManualUpEvent);
        });
        
        btnSeekDown.setOnLongClickListener(v -> {
            execRemote(IRadioServiceAPI::onSeekDownEvent); // V9: Swapped
            return true;
        });

        // Loop Band Logic
        btnBand.setOnClickListener(v -> {
             execRemote(IRadioServiceAPI::onBandEvent);
        });
    }

    private void bindPresetViews() {
        int[] cardIds = {R.id.cardP1, R.id.cardP2, R.id.cardP3, R.id.cardP4, R.id.cardP5, R.id.cardP6};
        int[] tvIds = {R.id.tvP1, R.id.tvP2, R.id.tvP3, R.id.tvP4, R.id.tvP5, R.id.tvP6};
        int[] ivIds = {R.id.ivP1, R.id.ivP2, R.id.ivP3, R.id.ivP4, R.id.ivP5, R.id.ivP6};

        for(int i=0; i<6; i++) {
            cardPresets[i] = findViewById(cardIds[i]);
            tvPresets[i] = findViewById(tvIds[i]);
            ivPresets[i] = findViewById(ivIds[i]);
        }
    }

    private void refreshPresetButtons() {
        for(int i=0; i<6; i++) {
            // Key is now P1_B0, P1_B1, etc.
            String key = "P" + (i+1) + "_B" + mCurrentBand;
            setupPresetCard(i, key);
        }
    }

    private void setupPresetCard(int index, String key) {
        int savedFreq = mPrefs.getInt(key, 0);
        updateCardVisuals(index, savedFreq);

        cardPresets[index].setOnClickListener(v -> {
            int freq = mPrefs.getInt(key, 0);
            if (freq > 0) {
                execRemote(s -> s.gotoFreq(freq));
            } else {
                showToast("Vacío - Mantén para guardar");
            }
        });

        cardPresets[index].setOnLongClickListener(v -> {
            if (mRadioService != null) {
                try {
                    int current = mRadioService.getCurrentFreq();
                    mPrefs.edit().putInt(key, current).apply();
                    updateCardVisuals(index, current);
                    // showToast("Guardado en B" + (mCurrentBand + 1)); // V8.4: Disabled toast
                } catch (RemoteException e) {}
            }
            return true;
        });
    }

    private void updateCardVisuals(int index, int freq) {
        if (freq == 0) {
            tvPresets[index].setText("Empty");
            ivPresets[index].setImageDrawable(null);
            tvPresets[index].setVisibility(View.VISIBLE);
            return;
        }

        // Default: Show Freq
        tvPresets[index].setText(String.format("%.1f", freq / 1000.0));
        ivPresets[index].setImageDrawable(null);
        tvPresets[index].setVisibility(View.VISIBLE);

        // Async Fetch Logo
        mRepository.getStationInfo(freq, logoUrl -> {
            runOnUiThread(() -> {
                if (logoUrl != null) {
                    Glide.with(MainActivity.this)
                         .load(logoUrl)
                         .transition(DrawableTransitionOptions.withCrossFade())
                         .into(ivPresets[index]);
                    // User Request: Logo Left, Name/Freq Right. Both Visible.
                    // Keep tvPresets VISIBLE but maybe update text to Name?
                }
            });
        });
        
        com.example.openradiofm.data.model.RadioStation s = mRepository.getStationInfo(freq, null);
        if (s.getName() != null && !s.getName().isEmpty()) {
             tvPresets[index].setText(s.getName());
        }
    }

    private interface RemoteAction { void run(IRadioServiceAPI s) throws RemoteException; }
    private void execRemote(RemoteAction action) {
        if (mRadioService == null) return;
        try { action.run(mRadioService); } catch (RemoteException e) { e.printStackTrace(); }
    }

    private void conectarRadio() {
        Intent intent = new Intent("com.hcn.autoradio.FM_PLUG_SERVICE");
        intent.setPackage("com.hcn.autoradio");
        try { bindService(intent, mConnection, Context.BIND_AUTO_CREATE); } catch (Exception e) {}
    }

    /**
     * Lee el estado actual de la radio desde el servicio remoto y lo refleja en la UI.
     *
     * IMPORTANTE:
     * - Este método puede ser llamado desde el hilo del Timer (segundo plano).
     * - Cualquier acceso a vistas se encapsula en runOnUiThread().
     */
    private void refreshRadioStatus() {
        if (mRadioService == null) return;
        execRemote(s -> {
            int freq = s.getCurrentFreq();
            int band = s.getCurrentBand();
            boolean isStereo = s.IsStereo();
            boolean isLocal = s.IsDxLocal();
            
            // Check if band changed -> Refresh presets
            if (band != mCurrentBand) {
                mCurrentBand = band;
                runOnUiThread(() -> refreshPresetButtons());
            }

            if (freq != mLastFreq) {
                mLastFreq = freq;
            }
            
            // SMART MAIN DISPLAY LOGIC
            mRepository.getStationInfo(freq, logoUrl -> {}); // Prefetch

            com.example.openradiofm.data.model.RadioStation station = mRepository.getStationInfo(freq, null);
            String rdsName = station.getName();
            
            runOnUiThread(() -> {
                // FIXED LOGIC: Always show Frequency in Big Box con 2 decimales
                tvFrequency.setText(String.format("%.2f", freq / 1000.0));
                // tvFrequency.setTextSize(110); // V8.4: Moved to XML
                
                if (rdsName != null && !rdsName.isEmpty()) {
                    tvRdsName.setText(rdsName);
                    // tvRdsInfo contains scrolling text (RT)
                } else {
                    tvRdsName.setText(""); 
                }
                
                // Ensure Sintonizando isn't blocking RDS Name if we have one
                if (rdsName != null && !rdsName.isEmpty()) {
                     if (!tvRdsName.getText().toString().equals(rdsName)) {
                         tvRdsName.setText(rdsName);
                     }
                }
                

                
                // CONDITIONAL LOGO VISIBILITY: Only show if RDS Name is present
                ImageView ivMainLogo = findViewById(R.id.ivMainLogo);
                if (rdsName != null && !rdsName.isEmpty()) {
                    ivMainLogo.setVisibility(View.VISIBLE);
                    mRepository.getStationInfo(freq, url -> {
                        runOnUiThread(() -> {
                            // V8.4: Logo Caching to prevent flickering
                            if (url != null) {
                                if (url.equals(mLastLogoUrl)) return; // Skip if same
                                mLastLogoUrl = url;
                                Glide.with(MainActivity.this)
                                     .load(url)
                                     .transition(DrawableTransitionOptions.withCrossFade())
                                     .into(ivMainLogo);
                            } else {
                                mLastLogoUrl = "";
                                // V9: Default Logo
                                ivMainLogo.setImageResource(R.mipmap.ic_launcher);
                            }
                        });
                    });
                } else {
                    // V9: Show Default Logo instead of Invisible? Or keep invisible?
                    // User said: "default if no logo... or until there is". 
                    // So if NO RDS Name found yet (Sintonizando...), show default?
                    ivMainLogo.setVisibility(View.VISIBLE);
                    ivMainLogo.setImageResource(R.mipmap.ic_launcher);
                }
                
                updateBandImage(band);
                btnLocDx.setSelected(isLocal);
                btnLocDx.setImageResource(isLocal ? R.drawable.radio_loc_p : R.drawable.radio_loc_n);
            });
        });
    }

    private void updateBandImage(int band) {
        int resId = R.drawable.radio_fm1; // Default
        if (band == 0) resId = R.drawable.radio_fm1;
        else if (band == 1) resId = R.drawable.radio_fm2;
        else if (band == 2) resId = R.drawable.radio_fm3;
        else if (band == 3) resId = R.drawable.radio_fm1; 
        else if (band == 4) resId = R.drawable.radio_fm2;
        
        btnBand.setImageResource(resId);
    }

    private void updateStatusIndicator(TextView tv, boolean active) {
        if (tv == null) return;
        if (active) {
            tv.setTextColor(Color.parseColor("#FF8C00")); // Orange for Active
            tv.setAlpha(1.0f);
        } else {
            tv.setTextColor(Color.parseColor("#FFFFFF"));
            tv.setAlpha(0.3f);
        }
    }

    private String getBandLabel(int bandCode) {
        if (bandCode == 0) return "FM 1";
        if (bandCode == 1) return "FM 2";
        if (bandCode == 2) return "FM 3";
        if (bandCode == 3) return "AM 1";
        if (bandCode == 4) return "AM 2";
        return "B" + bandCode;
    }

    private void showToast(String msg) {
        runOnUiThread(() -> Toast.makeText(this, msg, Toast.LENGTH_SHORT).show());
    }

    private void launchExternalApp(String packageName) {
        Intent intent = getPackageManager().getLaunchIntentForPackage(packageName);
        if (intent != null) {
            startActivity(intent);
        } else {
            showToast("App no instalada: " + packageName);
        }
    }
    
    /**
     * Muestra un diálogo para seleccionar el skin (tema de color) de la aplicación.
     * Permite cambiar entre: Classic Gray, Orange, Blue, Green, Purple.
     */
    private void showSkinSelectorDialog() {
        com.example.openradiofm.ui.theme.ThemeManager themeManager = 
            new com.example.openradiofm.ui.theme.ThemeManager(this);
        
        com.example.openradiofm.ui.theme.ThemeManager.Skin currentSkin = themeManager.getCurrentSkin();
        
        // Nombres de los skins para mostrar en el diálogo
        com.example.openradiofm.ui.theme.ThemeManager.Skin[] skins = 
            com.example.openradiofm.ui.theme.ThemeManager.Skin.values();
        String[] skinNames = new String[skins.length];
        int selectedIndex = 0;
        
        for (int i = 0; i < skins.length; i++) {
            skinNames[i] = skins[i].displayName;
            if (skins[i] == currentSkin) {
                selectedIndex = i;
            }
        }
        
        // Crear y mostrar el diálogo
        new android.app.AlertDialog.Builder(this)
            .setTitle("Seleccionar Tema de Color")
            .setSingleChoiceItems(skinNames, selectedIndex, (dialog, which) -> {
                // Guardar el skin seleccionado
                themeManager.setSkin(skins[which]);
                // Aplicar inmediatamente sin reiniciar
                applySkin(skins[which]);
                showToast("Tema aplicado: " + skins[which].displayName);
                dialog.dismiss();
            })
            .setNegativeButton("Cancelar", null)
            .show();
    }
    
    /**
     * Aplica el skin seleccionado a todos los elementos de la interfaz.
     */
    private void applySkin(com.example.openradiofm.ui.theme.ThemeManager.Skin skin) {
        int drawableId;
        switch (skin) {
            case ORANGE: drawableId = R.drawable.bg_glass_card_orange; break;
            case BLUE: drawableId = R.drawable.bg_glass_card_blue; break;
            case GREEN: drawableId = R.drawable.bg_glass_card_green; break;
            case PURPLE: drawableId = R.drawable.bg_glass_card_purple; break;
            case RED: drawableId = R.drawable.bg_glass_card_red; break;
            case YELLOW: drawableId = R.drawable.bg_glass_card_yellow; break;
            case CYAN: drawableId = R.drawable.bg_glass_card_cyan; break;
            case PINK: drawableId = R.drawable.bg_glass_card_pink; break;
            case WHITE: drawableId = R.drawable.bg_glass_card_white; break;
            default: drawableId = R.drawable.bg_glass_card_classic; break;
        }
        
        int[] viewIds = {
            R.id.boxFrequency, R.id.btnSeekUp, R.id.btnSeekDown,
            R.id.tvRdsName, R.id.tvRdsInfo,
            R.id.btnBand, R.id.btnAutoScan,
            R.id.boxLogo,
            R.id.btnLocDx, R.id.btnMute, R.id.btnSettings, R.id.btnTest,
            // Presets
            R.id.cardP1, R.id.cardP2, R.id.cardP3, R.id.cardP4, R.id.cardP5, R.id.cardP6
        };
        
        for (int id : viewIds) {
            android.view.View v = findViewById(id);
            if (v != null) v.setBackgroundResource(drawableId);
        }
    }
}
