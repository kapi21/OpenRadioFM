# ROADMAP v.5.0 & THE FUTURE - OpenRadioFM 🗺️

Tras la culminación de la **v.4.7.5 "Global Edition"**, los siguientes hitos de desarrollo establecen la ruta técnica y funcional para la versión 5.0, priorizando la arquitectura y la integración profunda con Android Automotive.

## 🚀 Fase 1: Desacoplamiento Arquitectónico (Clean Architecture)
- **Refactorización de `MainActivity`**: Reducir sus >2400 líneas migrando hacia Patrones de Presentación (MVVM o controladores dedicados).
- **Gestión Reactiva**: Remplazar el complejo sistema de callbacks de UI por observadores de estado.
- **`PresetUIController` y `MediaSessionControl`**: Extraer la lógica de favoritos y gestión del foco de audio (Bluetooth) a componentes aislados.

## 🛡️ Fase 2: Robustez del Motor (Service-Based)
- **`RadioForegroundService`**: Independizar el motor de radio (K706, MT8163, QS6) de la actividad principal. Esto asegurará que la radio nunca muera cuando el coche se quede sin memoria o se cambie a navegadores pesados.
- **Soporte `MediaSessionCompat` avanzado**: Permitirá a los controles del volante (SWC) y minireproductores del coche (pantalla dividida, PiP launcher) interactuar sin que la App principal esté abierta en pantalla.
- **Unificación de motores `BaseRadioEngine`**: Factorizar la infraestructura común de las plataformas para simplificar adición de futuros SoCs.

## 🌍 Fase 3: Ecosistema y Networking
- **Sistema de Logos Expandido (Europa+)**: Continuar potenciando el Crowdsourcing para cubrir PI Codes de Francia, UK, Alemania, Italia y Portugal en Supabase.
- **Modo Radio Híbrido**: Transición automática (Trigger de pérdida de señal RDS/RSSI o AF pasivo) del canal FM/AM a Online Streaming cuando el coche entra en un túnel.
- **Kotlin Migration (Fase Piloto)**: Iniciar adopción de Coroutines en Repositorios (Supabase/RadioBrowser) para reducir la complejidad técnica de los procesos multi-hilo de Java (`ExecutorService`).

## 💎 Características Premium y Experiencia
- **Soporte Nativo Android Auto**: Preparar los motores para emitir de forma universal a consolas conectadas externas usando la arquitectura Service-Based.
- **Liquid UI Animations**: Añadir transiciones interpoladas suaves entre Vistas (Layout 1, 2, 3) y al navegar por favoritos.
- **Analizador Espectro DSP**: Interceptar audio interno del chip principal para visualizar la banda sonora en tiempo real en la pantalla.

---
*OpenRadioFM: Elevando el estándar del In-Car Infotainment Libre.*
