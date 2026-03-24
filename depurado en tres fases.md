# Depurado en tres fases

## Fase 1
- Estabilidad de ciclo de vida: limpieza más segura de recursos en `RadioMediaService`.
- Liberación correcta de conexiones en `RadioServiceController`.
- Ajustes iniciales de hardening sin cambiar comportamiento funcional.

## Fase 2
- Concurrencia más robusta: caches de logos en `ConcurrentHashMap`.
- Tracking pendiente atómico en `RadioRepository`.
- Rendimiento mejorado: eliminación de `Thread.sleep(700)` bloqueante en flujo Supabase, manteniendo indicador visual sin bloquear hilos.
- Build más limpia: `BuildConfig` habilitado explícitamente y DSL de firma actualizado.
- Logging más limpio: `DEBUG_FETCH` solo en debug.

## Fase 3
- Estandarización de logs de excepción a `Log.e(..., e)` en clases núcleo.
- Mejor trazabilidad para depuración (stacktraces completos y consistentes).
- Limpieza final de ramas de trabajo tras merge por PR.

## Resultado final
- Cambios pequeños, probados y mergeados por fases.
- `main` quedó estable, limpia y sincronizada con remoto.
