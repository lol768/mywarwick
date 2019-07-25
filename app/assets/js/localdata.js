/* eslint-env browser */

import localforage from 'localforage';
import log from 'loglevel';

/*
 Wrapper for localforage because of reasons.

 We sometimes want to set a different driver for localforage, BUT
 the default instance of localforage is automatically created with
 the default drivers and there's a race to config it. Instead we'll
 use createInstance and mostly ignore the default instance.
*/

function isWebKit() {
  // Anything on iOS, otherwise anything that says it's Safari that isn't Chrome
  const ua = navigator.userAgent;
  const has = str => ua.indexOf(str) > -1;
  return has('iP') || (has('Safari') && !has('Chrome'));
}

const config = { name: 'Start' };

const webkit = isWebKit();

if (webkit) {
  // Safari's IndexedDB has rampant disk usage.
  log.info('Detected buggy AppleWebKit; using WebSQL instead of IndexedDB.');
  // Provide a list, just in case we get UA detection wrong, or Safari drops WebSQL support.
  // localforage will pick the first one that works.
  // Not including INDEXEDDB here for a couple of reasons:
  //  - We don't know that WebKit's bug will have been fixed
  //  - We'd be constantly deleting its database in the below code
  config.driver = [
    localforage.WEBSQL,
    localforage.LOCALSTORAGE,
  ];

  // Delete any existing IndexedDB to free space (successful noop if nonexistent).
  // Wait for default localforage to init and close its DB first, otherwise the delete
  // operation may be pending forever while it waits for a connection to close.
  // (Having thought about it a bit, we probably _don't_ need to wait for localforage
  // to be ready since it'll be created a default one with a different name - localforage -
  // but no harm at all in waiting.
  localforage.ready()
    .then(() => {
      if (localforage._dbInfo && localforage._dbInfo.db) {
        localforage._dbInfo.db.close();
      }
      if (window.indexedDB) {
        log.info(`Deleting any existing IndexedDB called ${config.name}`);
        const req = indexedDB.deleteDatabase(config.name);
        req.onsuccess = () => log.debug(`✓ IndexedDB ${config.name} deleted (or didn't exist)`);
        req.onerror = e => log.warn(`✗ IndexedDB ${config.name} deletion error`, e);
        req.onblocked = e => log.warn(`✗ Request to delete IndexedDB called ${config.name} is being blocked.`, e);
      }
    }).catch((e) => {
      log.warn(`✗ IndexedDB ${config.name} deletion error`, e);
    });
}

const instance = localforage.createInstance(config);

instance.ready()
  .then(() => {
    if (webkit && instance.driver() !== localforage.WEBSQL) {
      log.warn(`Tried to force WebSQL but instead we've got ${instance.driver()}`);
    }
  })
  .catch(err => log.warn(`Error opening localstorage: ${err.message}`));

export default instance;
