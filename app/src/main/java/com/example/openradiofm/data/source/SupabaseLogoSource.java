package com.example.openradiofm.data.source;

import com.example.openradiofm.BuildConfig;
import com.example.openradiofm.data.source.network.SupabaseApi;
import com.example.openradiofm.data.source.network.SupabaseClient;
import com.example.openradiofm.data.source.network.model.SupabaseLogoResponse;
import java.io.File;
import java.nio.file.Files;
import java.util.List;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Response;

import com.example.openradiofm.util.AppIoExecutor;

/**
 * Fuente de logos centralizada en Supabase.
 */
public class SupabaseLogoSource {
    private final SupabaseApi api;
    /** Inyectado en compilación (BuildConfig); override con SUPABASE_* en local.properties. */
    private final String apiKey = BuildConfig.SUPABASE_ANON_KEY;
    private final String storageUrl = BuildConfig.SUPABASE_STORAGE_PUBLIC_LOGOS_BASE;

    public SupabaseLogoSource() {
        this.api = SupabaseClient.getApi();
    }

    private static String folderForCountry(String countryCode) {
        String c = (countryCode != null) ? countryCode.trim().toUpperCase() : "OT";
        switch (c) {
            case "ES": return "espana";
            case "PT": return "portugal";
            case "RU": return "rusia";
            case "TR": return "turquia";
            case "GR": return "grecia";
            case "IT": return "italia";
            case "RO": return "romania";
            case "PL": return "polonia";
            case "HU": return "hungria";
            case "FR": return "francia";
            case "SI": return "eslovenia";
            case "OT":
            default: return "otros";
        }
    }

    /**
     * V18.8: Lista negra de nombres genéricos que ensucian la base de datos o causan búsquedas erróneas.
     */
    private static final String[] BLACKLIST_NAMES = {
            "FM", "RADIO", "STEREO", "RDS", "BUSCANDO", "SCAN", "TUNING", "SINR", "NO RDS", "EMPTY", "WAITING", "SIGNAL"
    };

    /**
     * PS inválido tipo buffer vacío (solo '0', longitud típica RDS/OEM NWD).
     * No persistir ni usar en consultas Supabase.
     */
    public static boolean isGarbageZeroPs(String name) {
        if (name == null) return false;
        String t = name.trim();
        if (t.length() < 4) return false;
        for (int i = 0; i < t.length(); i++) {
            if (t.charAt(i) != '0') return false;
        }
        return true;
    }

    public static boolean isNameGeneric(String name) {
        if (name == null || name.trim().length() < 2) return true;
        if (isGarbageZeroPs(name)) return true;
        String clean = name.trim().toUpperCase();
        for (String black : BLACKLIST_NAMES) {
            if (clean.equals(black)) return true;
        }
        return false;
    }

    /**
     * PS que parece ruido (casi solo dígitos / símbolos), típico de buffers o basura RDS.
     */
    public static boolean isMostlyDigitsOrNoise(String name) {
        if (name == null) return false;
        String t = name.trim();
        if (t.length() < 4) return false;
        int letters = 0;
        for (int i = 0; i < t.length(); i++) {
            if (Character.isLetter(t.charAt(i))) letters++;
        }
        if (letters >= 2) return false;
        return true;
    }

    /**
     * PS aceptable para publicar en la base comunitaria (nombre solo, sin PI).
     */
    public static boolean isPsAcceptableForPublishing(String name) {
        if (name == null) return false;
        String t = name.trim();
        if (t.length() < 5) return false;
        if (isGarbageZeroPs(t)) return false;
        if (isNameGeneric(t)) return false;
        if (isMostlyDigitsOrNoise(t)) return false;
        return true;
    }

    /**
     * ¿Tiene sentido un upsert con estos metadatos? (PI válido, o PS que pasa calidad mínima).
     */
    public static boolean isAcceptableForCloudUpsert(String piCode, String rdsName) {
        String pi = (piCode != null) ? piCode.trim() : "";
        if (!pi.isEmpty()) return true;
        String n = (rdsName != null) ? rdsName.trim() : "";
        return isPsAcceptableForPublishing(n);
    }

    /**
     * PS seguro para enviar a Supabase: null si no debe persistirse el nombre (basura o corto).
     * Con PI válido se puede devolver null y el upsert seguirá solo con PI.
     */
    public static String sanitizePsForCloudUpsert(String piCode, String rdsName) {
        if (rdsName == null) return null;
        String t = rdsName.trim();
        if (t.isEmpty()) return null;
        if (isGarbageZeroPs(t)) return null;
        if (isNameGeneric(t)) return null;
        if (isMostlyDigitsOrNoise(t)) return null;
        String pi = (piCode != null) ? piCode.trim() : "";
        int minLen = pi.isEmpty() ? 5 : 4;
        if (t.length() < minLen) return null;
        return t;
    }

    public SupabaseApi getSupabaseApi() {
        return api;
    }

    public String getApiKey() {
        return apiKey;
    }

    public interface DataActivityListener {
        void onDataActivity(boolean active);
    }

    private DataActivityListener mActivityListener;

    public void setDataActivityListener(DataActivityListener mActivityListener) {
        this.mActivityListener = mActivityListener;
    }

    public void notifyActivity(boolean active) {
        // android.util.Log.d("SupabaseLogoSource", "notifyActivity: active=" + active);
        if (mActivityListener != null) {
            try {
                mActivityListener.onDataActivity(active);
            } catch (Exception e) {
                android.util.Log.e("SupabaseLogoSource", "Error notifying activity", e);
            }
        }
    }
    /**
     * Busca el logo en Supabase siguiendo el orden de prioridad.
     * V16.2: Ahora filtra también por país si está disponible.
     */
    public String fetchLogo(String piCode, String rdsName, int freqKHz) {
        notifyActivity(true);
        // Preferencia explícita del usuario (más fiable que Locale en muchas head units)
        String country = com.example.openradiofm.utils.CountryPrefs.DEFAULT_COUNTRY;
        try {
            // No tenemos Context aquí; los fetch directos desde Repository ya pasan country.
            // Este fetch se usa en rutas antiguas; mantener fallback a Locale.
            String c = java.util.Locale.getDefault().getCountry();
            if (c != null && !c.isEmpty()) country = c;
        } catch (Exception ignored) {}
        if (country == null || country.isEmpty()) country = com.example.openradiofm.utils.CountryPrefs.DEFAULT_COUNTRY;
        country = country.toUpperCase();
        
        android.util.Log.d("SupabaseLogoSource", "FETCH START: PI=" + piCode + ", Name=" + rdsName + ", Freq=" + freqKHz + ", Country=" + country);
        try {
            // 1. prioridad: PI Code (exacto)
            if (piCode != null && !piCode.isEmpty()) {
                String logo = queryByPi(piCode, country);
                if (logo != null) {
                    android.util.Log.d("SupabaseLogoSource", "FETCH SUCCESS (PI): " + logo);
                    return logo;
                }
            }

            // 2. prioridad: RDS Name (exacto)
            if (rdsName != null && !rdsName.isEmpty()) {
                String trimmedName = rdsName.trim();
                if (!isNameGeneric(trimmedName)) {
                    String logo = queryByName(trimmedName, country);
                    if (logo != null) {
                        android.util.Log.d("SupabaseLogoSource", "FETCH SUCCESS (Name): " + logo);
                        return logo;
                    }
                } else {
                    android.util.Log.d("SupabaseLogoSource", "FETCH SKIP: Name '" + trimmedName + "' is generic.");
                }
            }

            // 3. prioridad: Frecuencia (kHz)
            String logoFreq = queryByFreq(freqKHz, country);
            if (logoFreq != null) {
                android.util.Log.d("SupabaseLogoSource", "FETCH SUCCESS (Freq): " + logoFreq);
            } else {
                android.util.Log.d("SupabaseLogoSource", "FETCH EMPTY: No logo found for this station.");
            }
            return logoFreq;
        } catch (Throwable e) {
            android.util.Log.e("SupabaseLogoSource", "FETCH ERROR", e);
            return null;
        } finally {
            notifyActivity(false);
        }
    }

    /**
     * V16.2: Envía o actualiza los datos de una emisora en el servidor centralizado.
     * V18.5: Añadido soporte para Storage y UserId.
     */
    public void upsertLogoData(android.content.Context context, String piCode, String rdsName, int freqKHz, String logoUrl, String streamUrl) {
        String piNorm = (piCode != null) ? piCode.trim() : "";
        String psForDb = sanitizePsForCloudUpsert(piNorm, rdsName);
        if (!isAcceptableForCloudUpsert(piNorm, rdsName)) {
            android.util.Log.d("SupabaseLogoSource", "UPSERT ABORT: quality gate (PI/name)");
            return;
        }
        if (piNorm.isEmpty() && (psForDb == null || psForDb.isEmpty())) return;

        final String fPi = piNorm.isEmpty() ? null : piNorm;
        final String fPs = psForDb;

        AppIoExecutor.execute(() -> {
            notifyActivity(true);
            try {
                // V18.6.3: ApplicationContext antes de cualquier uso (hilo de fondo + subida Storage).
                android.content.Context appContext = (context != null) ? context.getApplicationContext() : null;

                String finalLogoUrl = logoUrl;
                // V19.4: Formatear frecuencia a String MHz (ej: 87.50) para coincidir con SQL
                String freqStr = String.format(java.util.Locale.US, "%.2f", freqKHz / 1000.0);

                // Si el logo es local y existe, primero lo subimos al Storage de Supabase
                if (logoUrl != null && logoUrl.startsWith("file://")) {
                    // Nunca persistir rutas locales (file://...) en la base comunitaria.
                    // Si el upload falla, preferimos guardar solo metadatos (PI/PS/freq/stream) sin logo.
                    finalLogoUrl = null;
                    String localPath = logoUrl.substring(7);
                    File file = new File(localPath);
                    if (file.exists()) {
                        String fileName = (fPi != null ? fPi : (fPs != null ? fPs.replaceAll("[^a-zA-Z0-9]", "") : "station"))
                                + "_" + freqKHz + ".png";
                        
                        // Reubicar por país para mantener el bucket ordenado.
                        String countryForFolder = com.example.openradiofm.utils.CountryPrefs.getCountry(appContext != null ? appContext : context);
                        String folder = folderForCountry(countryForFolder);
                        String storagePath = folder + "/" + fileName;
                        
                        RequestBody requestBody = RequestBody.create(file, MediaType.parse("image/png"));
                        Call<Void> uploadCall = api.uploadLogoFile(apiKey, "Bearer " + apiKey, storagePath, requestBody);
                        Response<Void> uploadRes = uploadCall.execute();
                        
                        if (uploadRes.isSuccessful() || uploadRes.code() == 409) { // 409 duplicated is okay for us
                            finalLogoUrl = storageUrl + storagePath;
                            android.util.Log.d("SupabaseLogoSource", "UPLOAD SUCCESS: " + finalLogoUrl);
                        } else {
                            String errorBody = "";
                            try {
                                if (uploadRes.errorBody() != null) {
                                    errorBody = uploadRes.errorBody().string();
                                }
                            } catch (Exception ignored) {}
                            android.util.Log.e("SupabaseLogoSource", "UPLOAD FAILED: " + uploadRes.code() + " Error=" + errorBody);
                        }
                    }
                }

                String hwModel = android.os.Build.MODEL; // Identificador del hardware (ej: K706, etc)
                String deviceId = com.example.openradiofm.utils.DeviceMetadataUtils.getUniqueDeviceId(appContext != null ? appContext : context);

                // Enriquecer con Stream URL si no viene definido (auto-populación de la base comunitaria)
                String finalStreamUrl = streamUrl;
                String country = com.example.openradiofm.utils.CountryPrefs.getCountry(appContext != null ? appContext : context);
                // Permitir "OT" (Otro) para no forzar ES si el usuario está fuera de lista.
                // En ese caso, el dato se etiqueta con OT y no contamina países existentes.

                if ((finalStreamUrl == null || finalStreamUrl.isEmpty()) && fPs != null && !isNameGeneric(fPs)) {
                    android.util.Log.d("SupabaseLogoSource", "SUPABASE ENRICH: Searching stream for " + fPs);
                    WebRadioSource webSource = new WebRadioSource();
                    
                    com.example.openradiofm.data.source.network.model.StationSearchResponse webStation = webSource.fetchStation(freqKHz, fPs, country);
                    if (webStation != null && webStation.getStreamUrl() != null) {
                        finalStreamUrl = webStation.getStreamUrl();
                        android.util.Log.d("SupabaseLogoSource", "SUPABASE ENRICH: Found stream -> " + finalStreamUrl);
                        
                        // También aprovechamos si no hay logoUrl para usar el favicon encontrado
                        if (finalLogoUrl == null || finalLogoUrl.isEmpty()) {
                            finalLogoUrl = webStation.getFavicon();
                        }
                    }
                }

                SupabaseLogoResponse data = new SupabaseLogoResponse(fPi, fPs, freqStr, finalLogoUrl, finalStreamUrl, hwModel, deviceId, country);

                String conflictColumns = (fPi != null && !fPi.isEmpty()) ? "pi_code,country_code" : "ps_name,country_code";
                
                // V19.4: Cabecera 'return=minimal,resolution=merge-duplicates' para asegurar compatibilidad con PostgREST
                Call<Void> call = api.upsertLogo(apiKey, "Bearer " + apiKey, "return=minimal,resolution=merge-duplicates", conflictColumns, data);
                retrofit2.Response<Void> response = call.execute();
                
                if (response.isSuccessful()) {
                    android.util.Log.d("SupabaseLogoSource", "UPSERT SUCCESS: " + fPs + " PI=" + fPi + " (" + freqStr + ")");
                } else {
                    String errorBody = "";
                    try {
                        if (response.errorBody() != null) {
                            errorBody = response.errorBody().string();
                        }
                    } catch (Exception ignored) {}
                    android.util.Log.e("SupabaseLogoSource", "UPSERT FAILED: Code=" + response.code() + " Error=" + errorBody);
                }
            } catch (Throwable e) {
                android.util.Log.e("SupabaseLogoSource", "Error upserting logo", e);
            } finally {
                notifyActivity(false);
            }
        });
    }

    private String queryByPi(String pi, String country) {
        try {
            Call<List<SupabaseLogoResponse>> call = api.getLogosByPi(apiKey, "Bearer " + apiKey, "ilike." + pi, "eq." + country, "*");
            Response<List<SupabaseLogoResponse>> res = call.execute();
            if (res.isSuccessful() && res.body() != null && !res.body().isEmpty()) {
                return res.body().get(0).getLogoUrl();
            }
        } catch (Exception ignored) {}
        return null;
    }

    private String queryByName(String name, String country) {
        try {
            Call<List<SupabaseLogoResponse>> call = api.getLogosByName(apiKey, "Bearer " + apiKey, "ilike." + name, "eq." + country, "*");
            Response<List<SupabaseLogoResponse>> res = call.execute();
            if (res.isSuccessful() && res.body() != null && !res.body().isEmpty()) {
                return res.body().get(0).getLogoUrl();
            }
        } catch (Exception ignored) {}
        // Fallback: secondary table fed from Radio-Browser snapshots
        try {
            Call<List<SupabaseLogoResponse>> call2 = api.getRadioBrowserByName(apiKey, "Bearer " + apiKey, "ilike." + name, "eq." + country, "*");
            Response<List<SupabaseLogoResponse>> res2 = call2.execute();
            if (res2.isSuccessful() && res2.body() != null && !res2.body().isEmpty()) {
                return res2.body().get(0).getLogoUrl();
            }
        } catch (Exception ignored) {}
        return null;
    }

    /**
     * Secondary lookup by stream URL (Radio-Browser catalog).
     * This is used only as a fallback and does not replace the main stations table.
     */
    public String fetchRadioBrowserLogoByStreamUrl(String streamUrl, String country) {
        if (streamUrl == null || streamUrl.trim().isEmpty()) return null;
        String c = (country == null || country.trim().isEmpty()) ? "ES" : country.trim();
        notifyActivity(true);
        try {
            Call<List<SupabaseLogoResponse>> call = api.getRadioBrowserByStreamUrl(
                    apiKey,
                    "Bearer " + apiKey,
                    "eq." + streamUrl.trim(),
                    "eq." + c,
                    "*"
            );
            Response<List<SupabaseLogoResponse>> res = call.execute();
            if (res.isSuccessful() && res.body() != null && !res.body().isEmpty()) {
                return res.body().get(0).getLogoUrl();
            }
        } catch (Throwable ignored) {
        } finally {
            notifyActivity(false);
        }
        return null;
    }

    private String queryByFreq(int freq, String country) {
        try {
            // V19.4: Formatear frecuencia a String MHz (ej: 87.50) para coincidir con SQL
            String freqStr = String.format(java.util.Locale.US, "%.2f", freq / 1000.0);
            Call<List<SupabaseLogoResponse>> call = api.getLogosByFreq(apiKey, "Bearer " + apiKey, "eq." + freqStr, "eq." + country, "*");
            Response<List<SupabaseLogoResponse>> res = call.execute();
            if (res.isSuccessful() && res.body() != null && !res.body().isEmpty()) {
                return res.body().get(0).getLogoUrl();
            }
        } catch (Exception ignored) {}
        return null;
    }

    public void checkConnection(java.util.function.Consumer<Boolean> callback) {
        AppIoExecutor.execute(() -> {
            try {
                // Usar health endpoint para evitar falsos negativos.
                Call<Void> call = api.health(apiKey);
                retrofit2.Response<Void> res = call.execute();
                callback.accept(res.isSuccessful());
            } catch (Throwable e) {
                callback.accept(false);
            }
        });
    }
}
