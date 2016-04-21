import Immutable from 'immutable';

import { registerReducer } from './reducers';
import store from './store';

const UPDATE_READY = 'update.ready';

export function updateReady() {
  return {
    type: UPDATE_READY,
  };
}

const initialState = Immutable.fromJS({
  isUpdateReady: false,
});

registerReducer('update', (state = initialState, action) => {
  switch (action.type) {
    case UPDATE_READY:
      return state.merge({
        isUpdateReady: true,
      });
    default:
      return state;
  }
});

let updateTimerInterval;

function onUpdateReady() {
  clearInterval(updateTimerInterval);
  store.dispatch(updateReady());
}

function updateTimer() {
  const { status, IDLE, UPDATEREADY } = window.applicationCache;

  if (status === IDLE) {
    clearInterval(updateTimerInterval);
  }

  if (status === UPDATEREADY) {
    onUpdateReady();
  }
}

export function displayUpdateProgress() {
  if ('applicationCache' in window && !('serviceWorker' in navigator)) {
    const cache = window.applicationCache;

    cache.addEventListener('updateready', onUpdateReady);

    // NEWSTART-269 :disapproval:
    if (updateTimerInterval) {
      clearInterval(updateTimerInterval);
    }
    updateTimerInterval = setInterval(updateTimer, 1000);

    if (cache.status === cache.UPDATEREADY) {
      onUpdateReady();
    } else if (cache.status === cache.IDLE) {
      cache.update();
    }
  }
}
