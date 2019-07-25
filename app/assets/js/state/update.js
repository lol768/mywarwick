/* eslint-env browser */

import fetch from 'isomorphic-fetch';
import log from 'loglevel';
import * as app from './app';

const UPDATE_READY = 'UPDATE_READY';

const initialState = {
  isUpdateReady: false,
};

export function updateReady() {
  return {
    type: UPDATE_READY,
  };
}

export function reducer(state = initialState, action) {
  switch (action.type) {
    case UPDATE_READY:
      return {
        ...state,
        isUpdateReady: true,
      };
    default:
      return state;
  }
}

let updateTimerInterval;

export function displayUpdateProgress(dispatch) {
  function onUpdateReady() {
    clearInterval(updateTimerInterval);

    fetch('/service/revision')
      .then(res => res.text())
      .then(rev => dispatch(app.updateAssets(rev)))
      .catch(e => log.error('Error fetching revision information', e))
      .then(() => dispatch(updateReady()));
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
