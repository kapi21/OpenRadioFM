package com.example.openradiofm.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utilidades para el procesamiento y limpieza de metadatos de radio (RDS e
 * ICY).
 * Ayuda a eliminar etiquetas técnicas y caracteres extraños de la
 * visualización.
 */
public class MetadataUtils {

    private static final Pattern ICY_TITLE_PATTERN = Pattern.compile("StreamTitle='(.*?)';");
    private static final Pattern RDS_GARBAGE_PATTERN = Pattern.compile("[\\x00-\\x1F\\x7F-\\x9F]"); // Caracteres de
                                                                                                    // control no
                                                                                                    // imprimibles

    /**
     * Limpia un texto RDS o ICY para mostrarlo al usuario.
     * 
     * @param raw El texto bruto recibido del hardware o stream.
     * @return El texto limpio y formateado.
     */
    public static String cleanRdsText(String raw) {
        if (raw == null || raw.isEmpty()) {
            return "";
        }

        String cleaned = raw;

        // 1. Manejar formato ICY (Streaming): StreamTitle='Artista - Cancion';
        if (cleaned.contains("StreamTitle='")) {
            Matcher matcher = ICY_TITLE_PATTERN.matcher(cleaned);
            if (matcher.find()) {
                cleaned = matcher.group(1);
            }
        }

        // 2. Eliminar caracteres de control (Basura RDS común en hardware)
        cleaned = RDS_GARBAGE_PATTERN.matcher(cleaned).replaceAll("");

        // 3. Normalizar comillas y caracteres especiales comunes
        cleaned = cleaned.replace("'", "").replace("\"", "").trim();

        // 4. Limpiar espacios duplicados
        cleaned = cleaned.replaceAll("\\s+", " ");

        // 5. Caso especial: Si el texto está en mayúsculas sostenidas, lo suavizamos
        // (Opcional)
        // Pero para RDS suele ser mejor dejarlo como viene si es corto.

        // 6. Si el resultado es una cadena técnica vacía como "url=" etc.
        if (cleaned.toLowerCase().startsWith("url=") || cleaned.equals("text=")) {
            return "";
        }

        return cleaned;
    }

    /**
     * Intenta separar Artista y Canción si el texto sigue el formato "Artista -
     * Cancion".
     * Útil si quisiéramos mostrarlos en dos líneas diferentes.
     */
    public static String[] splitArtistTitle(String text) {
        if (text == null || !text.contains(" - ")) {
            return new String[] { text, "" };
        }
        return text.split(" - ", 2);
    }
}
