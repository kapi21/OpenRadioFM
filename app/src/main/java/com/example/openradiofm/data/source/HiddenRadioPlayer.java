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

    private Object mRadioPlayerInstance;
    private Listener mClientListener;

    public interface Listener {
        void onRdsText(String text);

        void onRdsName(String name);

        void onRawEvent(int code, Object infoObj, String strArg);
    }

    public HiddenRadioPlayer(Listener listener) {
        this.mClientListener = listener;
    }

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
            return true;

        } catch (Exception e) {
            Log.e(TAG, "Fallo al iniciar HiddenRadioPlayer: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

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

            if (code == 41) { // RT
                if (arg2 instanceof String) {
                    if (mClientListener != null)
                        mClientListener.onRdsText((String) arg2);
                } else if (arg2 == null) {
                    if (mClientListener != null)
                        mClientListener.onRdsText("");
                }
            }

            if (code == 38) { // PS
                if (arg2 instanceof String) {
                    if (mClientListener != null)
                        mClientListener.onRdsName((String) arg2);
                } else if (arg2 == null) {
                    if (mClientListener != null)
                        mClientListener.onRdsName("");
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "Error parseando evento: " + e.getMessage());
        }
    }
}
