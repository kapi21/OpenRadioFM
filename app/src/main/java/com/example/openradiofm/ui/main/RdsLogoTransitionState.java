package com.example.openradiofm.ui.main;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Invalidación de cargas de logo async y ventana post-cambio de frecuencia (QS6 / arrastre PS).
 */
final class RdsLogoTransitionState {
    final AtomicInteger logoUiGeneration = new AtomicInteger(0);
    volatile long rdsTransitionGuardUntilMs;
    volatile String prevStationNameBeforeTune = "";
}
