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
    public static final int EVENT_PS_DONE = 0x8;
    public static final int EVENT_PS_MESSAGE = 0x26;
    public static final int EVENT_RT_MESSAGE = 0x29;
    public static final int EVENT_PTY_TYPE = 0x22;

    private Object mRadioPlayerInstance;
    private Listener mClientListener;

    public interface Listener {
        void onRdsText(String text);

        void onRdsName(String name);
        void onRdsPty(String pty);
        void onRawEvent(int code, Object infoObj, String strArg);
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
            Log.e(TAG, "Fallo al iniciar HiddenRadioPlayer: " + e.getMessage());
            e.printStackTrace();
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
            Log.e(TAG, "Error llamando a stereo(): " + e.getMessage());
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
            Log.e(TAG, "Error llamando a setMute(): " + e.getMessage());
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

            // PS_DONE también puede indicar que el nombre está listo
            if (code == EVENT_PS_DONE) {
                 Log.d(TAG, "RDS PS_DONE received");
            }

        } catch (Exception e) {
            Log.e(TAG, "Error parseando evento: " + e.getMessage());
        }
    }

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
