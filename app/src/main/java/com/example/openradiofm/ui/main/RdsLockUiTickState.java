package com.example.openradiofm.ui.main;

/**
 * RDS “enganchado” en la UI + estado para el tick visual al flanco de lock (Fase 6 refactor).
 */
final class RdsLockUiTickState {
    boolean hasLock;
    boolean hadLockForTick;
    long lastTickUptimeMs;
}
