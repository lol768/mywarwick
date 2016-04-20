import { createSelector } from 'reselect';
import localforage from 'localforage';
import log from 'loglevel';

// Immutable object => plain JS object
const defaultFreeze = x => ((x !== undefined /* (or null) */ && 'toJS' in x) ? x.toJS() : x);

// Identity function
const defaultThaw = x => x;

/**
 * Factory method for injection of interesting dependencies.
 */
export default function init(opts) {
  const { store } = opts;

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
  return function persisted(keyPath, action, freeze = defaultFreeze, thaw = defaultThaw) {
    const keyPathArray = keyPath.split('.');
    if (!action) {
      throw new Error(`Action passed to persisted for ${keyPath} was undefined`);
    }

    // Load whatever we have in local storage
    return localforage.getItem(keyPath)
      .then(value => {
        if (value !== null) {
          this.store.dispatch(action(thaw(value)));
        }
      })
      .catch(err => log.warn(`Unable to load ${keyPath} from local storage`, err.stack || err))
      .then(() => {
        // Whenever the value at this key path changes
        const selector = createSelector(
          state => state.getIn(keyPathArray),
          value => localforage.setItem(keyPath, freeze(value))
        );

        this.store.subscribe(() => selector(store.getState()));
      });
  };
}


