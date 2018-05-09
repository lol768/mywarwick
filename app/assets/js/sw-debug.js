/* eslint-env browser */
/* global workbox */

/**
 * This code ends up in service-worker, so it wants to be as minimal as possible.
 * This includes not importing lots of libraries into it, because they'll all end
 * up in service-worker.js.
 */

// Increase workbox log levels
workbox.setConfig({
  debug: true,
});
workbox.core.setLogLevel(workbox.core.LOG_LEVELS.debug);
