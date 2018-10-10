import localforage from 'localforage';
import log from 'loglevel';

/*
 Wrapper for localforage because of reasons.

 We sometimes want to set a different driver for localforage, BUT
 the default instance of localforage is automatically created with
 the default drivers and there's a race to config it. Instead we'll
 use createInstance and mostly ignore the default instance.
*/

function isSafari() {
  // Only matches iOS Safari, not Mac OS Safari, but don't care
  const ua = navigator.userAgent;
  const has = (str) => ua.indexOf(str) > -1;
  return has('Safari') &&
    (!has('Chrome') || has('iP'));
}

const config = { name: 'Start' };

if (isSafari()) {
  // Safari's IndexedDB has rampant disk usage.
  log.info('Detected buggy Safari, using WebSQL instead of IndexedDB.');
  // Delete any existing IndexedDB to free space (successful noop if nonexistent).
  // Wait for default localforage to init and close its DB first, otherwise the delete
  // operation may be pending forever while it waits for a connection to close.
  localforage.ready().then(() => {
    if (localforage._dbInfo && localforage._dbInfo.db) {
      localforage._dbInfo.db.close();
    }
    if (window.indexedDB) {
      log.info('Deleting any existing IndexedDB');
      indexedDB.deleteDatabase('Start');
    }
  });
  config.driver = localforage.WEBSQL;
}

const instance = localforage.createInstance(config);

export default instance;