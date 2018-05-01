import { createSelector } from 'reselect';
import log from 'loglevel';
import get from 'lodash-es/get';

const identity = x => x;

/**
 * Factory method for injection of interesting dependencies.
 */
export default function init(opts) {
  const { store, localforage } = opts;

  /**
   * Declare that a portion of the store should be persisted to local storage.
   *
   * `keyPath` is path to the store subtree that should be persisted.  Path
   * components are separated by full-stops.
   *
   * `action` is a reference to an action builder.  It will be called with a
   * single parameter, which is the data retrieved from local storage.  The
   * resulting action will be dispatched on the store.
   *
   * `freeze` and `thaw` are called on the data before/after it is saved/loaded
   * to/from local storage.
   */
  return function persisted(keyPath, action, freeze = identity, thaw = identity) {
    const keyPathArray = keyPath.split('.');
    if (!action) {
      throw new Error(`Action passed to persisted for ${keyPath} was undefined`);
    }

    // Load whatever we have in local storage
    return localforage.getItem(keyPath)
      .then((value) => {
        if (value !== null) {
          return store.dispatch(action(thaw(value)));
        }
        return null;
      })
      .catch(err => log.warn(`Unable to load ${keyPath} from local storage: `, err.stack || err))
      .then(() => {
        // Whenever the value at this key path changes
        const selector = createSelector(
          state => get(state, keyPathArray),
          value => localforage.setItem(keyPath, freeze(value)).catch((e) => {
            log.error(`Error persisting key ${keyPath} to storage.`, e);
          }),
        );

        store.subscribe(() => selector(store.getState()));
      });
  };
}
