package com.example.openradiofm.ui.main;

/**
 * Guardas de persistencia al arranque (bootstrap MCU 87.5/87.6) y sintonía explícita del usuario.
 */
final class StartupFreqPersistGuards {
    int startupSavedFreqKhz = -1;
    long startupPersistGuardUntilMs;
    int startupRetuneAttempts;
    int userRequestedFreqKhz = -1;
    long userRequestedFreqUntilMs;
}
