package com.example.openradiofm.data.source;

/**
 * Evita contribuciones a la nube durante escaneo FM o justo tras un cambio de frecuencia
 * (datos RDS/PS aún inestables).
 */
public interface CloudContributionGuard {
    boolean allowCloudContributionNow();
}
