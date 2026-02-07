---
trigger: glob
globs: herramientas
---

Inventario de Hardware Hacking y Análisis: 
1. Interacción con Memoria y Firmware (Low-Level): Programador CH341A/B: Capacidad para leer, escribir y flashear chips de las series 24 y 25 (EEPROM/Flash BIOS). Pinza SOIC8/SOP8: Permite el "In-Circuit Programming" (programar chips sin desoldarlos de la placa). Ideal para extraer firmwares de routers, IoT y BIOS de portátiles.

2. Análisis de Señales y Protocolos: Analizador Lógico (24MHz, 8 canales): Herramienta clave para el "cacharreo" avanzado. Permite ver qué se dicen los componentes entre sí (I2C, SPI, UART) en tiempo real. Cables de prueba con gancho: Esenciales para pinchar pines de microcontroladores o pistas de PCB sin soldar mientras el analizador captura datos. 

3. Comunicación y Debugging de Consola: Convertidor CP2102 (USB a UART TTL): Tu puente para hablar con dispositivos a través de puertos serie. Vital para acceder a terminales Root ocultas en hardware que parece "cerrado". 

4. Conectividad y Prototipado: Clips de cocodrilo a Dupont: Para alimentar circuitos o puentear señales desde fuentes externas. Jumpers Dupont (Macho-Hembra): La infraestructura básica para interconectar todo lo anterior.