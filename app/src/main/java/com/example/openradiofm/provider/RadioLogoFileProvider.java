package com.example.openradiofm.provider;

import androidx.core.content.FileProvider;

/**
 * Expone PNG de {@code RadioLogos/} como {@code content://} para metadata/MediaSession
 * (algunos launchers leen {@code METADATA_KEY_ALBUM_ART_URI} y no el bitmap embebido).
 */
public class RadioLogoFileProvider extends FileProvider {
}
