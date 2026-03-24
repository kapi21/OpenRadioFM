package com.example.openradiofm.data.source;

import android.util.Log;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class HiddenRadioPlayer {
    private static final String TAG = "HiddenRadioPlayer";

    private static final String CLASS_RADIO_PLAYER = "android.radio.RadioPlayer";
    private static final String CLASS_LISTENER = "android.radio.RadioPlayer$OnEventListener";

    // RDS Event Codes identified in technical documentation
    public static final int EVENT_RDS_STATE = 0x4;
    public static final int EVENT_PS_DONE = 0x8;
    public static final int EVENT_PS_MESSAGE = 0x26;
    public static final int EVENT_RT_MESSAGE = 0x29;
    public static final int EVENT_PTY_TYPE = 0x22;

    // V4.6: Estado interno AF/TA/TP para MT8163
    private boolean mIsAfEnabled = false;
    private boolean mIsTaEnabled = false;
    private boolean mIsTpEnabled = false;

    private Object mRadioPlayerInstance;
    private Listener mClientListener;

    public interface Listener {
        void onRdsText(String text);
        void onRdsName(String name);
        void onRdsPty(String pty);
        void onRawEvent(int code, Object infoObj, String strArg);
        /** V4.6: Notifica cambios de estado AF/TA/TP al UI */
        void onRdsAfTaStatus(boolean afEnabled, boolean taEnabled, boolean tpEnabled);
    }

    /**
     * Capa de abstracción sobre la API oculta android.radio.RadioPlayer.
     * 
     * POR QUÉ REFLEXIÓN:
     * La clase android.radio.RadioPlayer es interna de la ROM del coche y no está en el SDK de Android.
     * Usamos reflexión (Class.forName) para cargarla dinámicamente solo si existe.
     * Esto evita que la app crashee en teléfonos móviles normales donde esta clase no existe.
     */
    public HiddenRadioPlayer(Listener listener) {
        this.mClientListener = listener;
    }

    /**
     * Intenta obtener una instancia de RadioPlayer y registrar un listener de eventos.
     *
     * IMPORTANTE:
     * - Debe llamarse desde un hilo de fondo o justo después de tener el servicio listo.
     * - Si el dispositivo no expone android.radio.RadioPlayer, simplemente devuelve false
     *   y no lanza la app.
     */
    public boolean init() {
        try {
            Class<?> radioPlayerClass = Class.forName(CLASS_RADIO_PLAYER);
            Class<?> listenerInterface = Class.forName(CLASS_LISTENER);

            Method getInstanceMethod = radioPlayerClass.getMethod("getRadioPlayer");
            mRadioPlayerInstance = getInstanceMethod.invoke(null);
            Log.d(TAG, "RadioPlayer obtenido vía getRadioPlayer().");

            Object proxyListener = Proxy.newProxyInstance(
                    listenerInterface.getClassLoader(),
                    new Class<?>[] { listenerInterface },
                    new InvocationHandler() {
                        @Override
                        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                            if (method.getName().equals("onEvent")) {
                                handleOnEvent(args);
                            }
                            return null;
                        }
                    });

            Method registerMethod = radioPlayerClass.getMethod("setOnEventListener", listenerInterface);
            registerMethod.invoke(mRadioPlayerInstance, proxyListener);

            Log.d(TAG, "Listener registrado con éxito.");
            
            // Recomendación PROBLEMA_STEREO.md: Forzar modo estéreo para habilitar auto-detección
            setStereo(true);
            
            return true;

        } catch (ClassNotFoundException e) {
            Log.e(TAG, "ESTE DISPOSITIVO NO ES COMPATIBLE: No se encontraron las clases de radio del sistema.");
            return false;
        } catch (Exception e) {
            Log.e(TAG, "Fallo al iniciar HiddenRadioPlayer", e);
            return false;
        }
    }

    /**
     * Fuerza o habilita el modo estéreo.
     * Según la documentación, llamar esto con true habilita la auto-detección del tono piloto.
     */
    public void setStereo(boolean enable) {
        if (mRadioPlayerInstance == null) return;
        try {
            Method stereoMethod = mRadioPlayerInstance.getClass().getMethod("stereo", boolean.class);
            stereoMethod.invoke(mRadioPlayerInstance, enable);
            Log.d(TAG, "setStereo(" + enable + ") ejecutado.");
        } catch (Exception e) {
            Log.e(TAG, "Error llamando a stereo()", e);
        }
    }

    /**
     * Control de silencio mediante la API directa del chip.
     */
    public void setMute(boolean mute) {
        if (mRadioPlayerInstance == null) return;
        try {
            Method muteMethod = mRadioPlayerInstance.getClass().getMethod("setMute", boolean.class);
            muteMethod.invoke(mRadioPlayerInstance, mute);
            Log.d(TAG, "setMute(" + mute + ") ejecutado.");
        } catch (Exception e) {
            Log.e(TAG, "Error llamando a setMute()", e);
        }
    }

    /**
     * Navegación nativa al siguiente favorito guardado en el chip.
     */
    public void next() {
        if (mRadioPlayerInstance == null) return;
        try {
            Method nextMethod = mRadioPlayerInstance.getClass().getMethod("next");
            nextMethod.invoke(mRadioPlayerInstance);
            Log.d(TAG, "next() ejecutado.");
        } catch (Exception e) {
            Log.e(TAG, "Error llamando a next()", e);
        }
    }

    /**
     * Navegación nativa al favorito anterior guardado en el chip.
     */
    public void prev() {
        if (mRadioPlayerInstance == null) return;
        try {
            Method prevMethod = mRadioPlayerInstance.getClass().getMethod("prev");
            prevMethod.invoke(mRadioPlayerInstance);
            Log.d(TAG, "prev() ejecutado.");
        } catch (Exception e) {
            Log.e(TAG, "Error llamando a prev()", e);
        }
    }

    /**
     * Procesa cada evento recibido desde la API interna del coche
     * y lo traduce a callbacks de alto nivel para la Activity.
     */
    private void handleOnEvent(Object[] args) {
        if (args == null || args.length < 2)
            return;

        try {
            int code = (Integer) args[0];
            Object arg2 = args[1];

            Log.d(TAG, "Event Recibido: " + code + " -> " + arg2);

            if (mClientListener != null) {
                String strDebug = (arg2 != null) ? arg2.toString() : "null";
                mClientListener.onRawEvent(code, arg2, strDebug);
            }

            // Mapeo basado en technical_docs (CHIP_RADIO_FM_FUNCIONES.md)
            if (code == EVENT_RT_MESSAGE || code == 41) { // 41 era el código anterior
                if (arg2 instanceof String) {
                    if (mClientListener != null)
                        mClientListener.onRdsText((String) arg2);
                } else if (arg2 == null) {
                    if (mClientListener != null)
                        mClientListener.onRdsText("");
                }
            }

            if (code == EVENT_PS_MESSAGE || code == 38) { // 38 era el código anterior
                if (arg2 instanceof String) {
                    if (mClientListener != null)
                        mClientListener.onRdsName((String) arg2);
                } else if (arg2 == null) {
                    if (mClientListener != null)
                        mClientListener.onRdsName("");
                }
            }

            if (code == EVENT_PTY_TYPE || code == 34) { // 0x22 = 34
                if (arg2 instanceof String) {
                    if (mClientListener != null)
                        mClientListener.onRdsPty((String) arg2);
                } else if (arg2 == null) {
                    if (mClientListener != null)
                        mClientListener.onRdsPty("");
                }
            }

            // V11.6: EVENT_RDS_STATE (0x04) contains TP/TA/AF status flags
            if (code == EVENT_RDS_STATE) {
                if (arg2 instanceof Integer) {
                    int state = (Integer) arg2;
                    // Bit 5 (0x20) is typically TP (Traffic Program) indicator in RDS status byte
                    boolean tp = (state & 0x20) != 0;
                    if (tp != mIsTpEnabled) {
                        mIsTpEnabled = tp;
                        if (mClientListener != null) mClientListener.onRdsAfTaStatus(mIsAfEnabled, mIsTaEnabled, mIsTpEnabled);
                    }
                }
            }

            // PS_DONE también puede indicar que el nombre está listo
            if (code == EVENT_PS_DONE) {
                 Log.d(TAG, "RDS PS_DONE received");
            }

        } catch (Exception e) {
            Log.e(TAG, "Error parseando evento", e);
        }
    }

    /**
     * V4.6: Toggle AF (Alternative Frequencies) via RadioPlayer.setAF(boolean).
     */
    public void setAF(boolean enable) {
        if (mRadioPlayerInstance == null) return;
        try {
            Method setAfMethod = mRadioPlayerInstance.getClass().getMethod("setAF", boolean.class);
            setAfMethod.invoke(mRadioPlayerInstance, enable);
            mIsAfEnabled = enable;
            Log.d(TAG, "setAF(" + enable + ") ejecutado.");
            if (mClientListener != null) mClientListener.onRdsAfTaStatus(mIsAfEnabled, mIsTaEnabled, mIsTpEnabled);
        } catch (Exception e) {
            Log.e(TAG, "Error llamando a setAF()", e);
        }
    }

    /**
     * V4.6: Toggle TA (Traffic Announcement) via RadioPlayer.setTA(boolean).
     */
    public void setTA(boolean enable) {
        if (mRadioPlayerInstance == null) return;
        try {
            Method setTaMethod = mRadioPlayerInstance.getClass().getMethod("setTA", boolean.class);
            setTaMethod.invoke(mRadioPlayerInstance, enable);
            mIsTaEnabled = enable;
            Log.d(TAG, "setTA(" + enable + ") ejecutado.");
            if (mClientListener != null) mClientListener.onRdsAfTaStatus(mIsAfEnabled, mIsTaEnabled, mIsTpEnabled);
        } catch (Exception e) {
            Log.e(TAG, "Error llamando a setTA()", e);
        }
    }

    /**
     * V4.6: Toggle RDS features para MT8163.
     * type 0: RDS global, 1: AF, 2: TA
     */
    public void toggleRdsFeature(int type) {
        switch (type) {
            case 1: // AF
                setAF(!mIsAfEnabled);
                break;
            case 2: // TA
                setTA(!mIsTaEnabled);
                break;
            case 0: // RDS global (futuro)
                Log.d(TAG, "RDS global toggle not yet implemented for MT8163");
                break;
            default:
                Log.w(TAG, "toggleRdsFeature: tipo desconocido " + type);
                break;
        }
    }

    /** V4.6: Getters de estado para la UI */
    public boolean isAfEnabled() { return mIsAfEnabled; }
    public boolean isTaEnabled() { return mIsTaEnabled; }
    public boolean isTpEnabled() { return mIsTpEnabled; }

    /**
     * Libera referencias para evitar fugas de memoria.
     */
    public void release() {
        // Intentar desregistrar el listener si es posible (Opcional, no garantizado por API interna)
        try {
            if (mRadioPlayerInstance != null) {
                Method registerMethod = mRadioPlayerInstance.getClass().getMethod("setOnEventListener", Class.forName(CLASS_LISTENER));
                registerMethod.invoke(mRadioPlayerInstance, new Object[]{null});
            }
        } catch (Exception ignored) {}
        
        mClientListener = null;
        mRadioPlayerInstance = null;
    }
}
