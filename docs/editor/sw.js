/**
 * Service worker mínimo para que Chrome/Android trate el editor como PWA instalable.
 * Las peticiones siguen yendo a red (sin caché offline agresiva).
 */
self.addEventListener('install', () => {
    self.skipWaiting();
});

self.addEventListener('activate', (event) => {
    event.waitUntil(self.clients.claim());
});

self.addEventListener('fetch', (event) => {
    event.respondWith(fetch(event.request));
});
