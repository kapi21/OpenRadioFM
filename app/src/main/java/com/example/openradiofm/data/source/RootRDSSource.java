package com.example.openradiofm.data.source;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class RootRDSSource {

    private int lastFreq = -1;
    private String lastRdsName = null;
    private long lastCheckTime = 0;

    private java.lang.Process mRootProcess;
    private java.io.DataOutputStream mDos;
    private java.io.BufferedReader mReader;

    // Constructor: inicializa el shell root en segundo plano.
    // Se deja preparado pero puede fallar en dispositivos sin root.
    public RootRDSSource() {
        initShell();
    }

    /**
     * Intenta abrir un shell root (su) y preparar los streams.
     * Si algo falla, deja los campos a null para que el llamante
     * pueda detectar que no hay root disponible sin romper la app.
     */
    private void initShell() {
        try {
            mRootProcess = Runtime.getRuntime().exec("su");
            mDos = new java.io.DataOutputStream(mRootProcess.getOutputStream());
            mReader = new java.io.BufferedReader(new java.io.InputStreamReader(mRootProcess.getInputStream()));
        } catch (Exception e) {
            // Si aquí falla, asumimos que el dispositivo NO tiene root o se ha denegado
            e.printStackTrace();
            mRootProcess = null;
            mDos = null;
            mReader = null;
        }
    }

    /**
     * Obtiene el nombre RDS desde el fichero XML interno del servicio de radio del coche.
     *
     * IMPORTANTE - SEGURIDAD Y RENDIMIENTO:
     * 1. Este método lee un archivo protegido del sistema (/data/data/...) usando "cat" vía root.
     * 2. DEBE ejecutarse en un HILO DE FONDO (background thread). Si se ejecuta en el hilo UI,
     *    bloqueará la interfaz de usuario causando "Application Not Responding" (ANR).
     * 3. Si no hay root, devuelve null de forma segura.
     * 4. TIMEOUT DE 2 SEGUNDOS: Si el shell root no responde, se cancela la operación.
     */
    public synchronized String getRdsName(int freqKHz) {
        long now = System.currentTimeMillis();
        if (freqKHz == lastFreq && (now - lastCheckTime) < 2000) {
            return lastRdsName;
        }

        try {
            if (mRootProcess == null) {
                initShell();
            }
            // Si seguimos sin proceso root disponible, devolvemos null silenciosamente.
            if (mRootProcess == null || mDos == null || mReader == null) {
                lastRdsName = null;
                return null;
            }

            lastFreq = freqKHz;
            lastCheckTime = now;

            mDos.writeBytes("cat /data/data/com.hcn.autoradio/shared_prefs/radio_rds.xml\n");
            mDos.writeBytes("echo __END__\n");
            mDos.flush();

            // Usar ExecutorService con timeout para evitar bloqueos infinitos
            java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors.newSingleThreadExecutor();
            java.util.concurrent.Future<String> future = executor.submit(() -> {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = mReader.readLine()) != null) {
                    if (line.trim().equals("__END__"))
                        break;
                    sb.append(line);
                }
                return sb.toString();
            });

            String xml;
            try {
                // Timeout de 2 segundos
                xml = future.get(2, java.util.concurrent.TimeUnit.SECONDS);
            } catch (java.util.concurrent.TimeoutException e) {
                android.util.Log.e("RootRDSSource", "Timeout leyendo shell root");
                future.cancel(true);
                executor.shutdownNow();
                lastRdsName = null;
                return null;
            } finally {
                executor.shutdown();
            }

            String search = "name=\"" + freqKHz + "\">";

            int start = xml.indexOf(search);
            if (start != -1) {
                start += search.length();
                int end = xml.indexOf("</string>", start);
                if (end != -1) {
                    lastRdsName = xml.substring(start, end);
                    return lastRdsName;
                }
            }
            lastRdsName = null;
            return null;

        } catch (Exception e) {
            // Cualquier error aquí implica que algo ha ido mal con el proceso root.
            // Dejamos los campos a null para forzar una reinicialización en la siguiente llamada.
            e.printStackTrace();
            mRootProcess = null;
            mDos = null;
            mReader = null;
            lastRdsName = null;
            return null;
        }
    }

    /**
     * Cierra limpiamente el proceso Root.
     * 
     * POR QUÉ ES NECESARIO:
     * Un proceso creado con Runtime.exec("su") se queda vivo en el sistema hasta que se cierra explícitamente.
     * Si no llamamos a esto al cerrar la app, acumularemos procesos "su" zombis que consumen memoria.
     * 
     * Se envía el comando "exit" y se destruye el objeto Process.
     */
    public synchronized void shutdown() {
        try {
            if (mDos != null) {
                mDos.writeBytes("exit\n");
                mDos.flush();
            }
        } catch (Exception ignored) {
        }

        try {
            if (mReader != null) {
                mReader.close();
            }
        } catch (Exception ignored) {
        }

        try {
            if (mDos != null) {
                mDos.close();
            }
        } catch (Exception ignored) {
        }

        if (mRootProcess != null) {
            mRootProcess.destroy();
        }

        mRootProcess = null;
        mDos = null;
        mReader = null;
    }
}
