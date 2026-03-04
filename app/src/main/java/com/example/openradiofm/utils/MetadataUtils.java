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
            java.util.regex.Matcher matcher = ICY_TITLE_PATTERN.matcher(cleaned);
            if (matcher.find()) {
                cleaned = matcher.group(1);
            }
        }

        // 2. Eliminar caracteres de control (Basura RDS común en hardware)
        // V16.2: Ampliado para cubrir más rangos de control y caracteres nulos
        cleaned = cleaned.replaceAll("[\\x00-\\x1F\\x7F-\\x9F]", "");

        // 3. Normalizar comillas, apóstrofes y secuencias de escape comunes
        cleaned = cleaned.replace("'", "")
                        .replace("\"", "")
                        .replace("\\r", "")
                        .replace("\\n", "")
                        .replace("\\t", "")
                        .trim();

        // 4. Limpiar espacios duplicados y caracteres raros al inicio/final
        cleaned = cleaned.replaceAll("\\s+", " ").trim();

        // 5. Caso especial: Filtrar prefijos técnicos de algunos encoders (ej: "RT:", "PS:")
        if (cleaned.toUpperCase().startsWith("RT:")) {
            cleaned = cleaned.substring(3).trim();
        }

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
