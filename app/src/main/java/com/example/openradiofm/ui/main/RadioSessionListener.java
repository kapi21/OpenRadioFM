package com.example.openradiofm.ui.main;

/**
 * Listener simple para observar cambios de estado de la sesión de radio.
 */
public interface RadioSessionListener {
    void onRadioSessionStateChanged(RadioSessionState state);
}

