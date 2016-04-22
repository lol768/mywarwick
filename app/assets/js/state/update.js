import Immutable from 'immutable';

const UPDATE_READY = 'UPDATE_READY';

const initialState = Immutable.fromJS({
  isUpdateReady: false,
});

export function updateReady() {
  return {
    type: UPDATE_READY,
  };
}

export function reducer(state = initialState, action) {
  switch (action.type) {
    case UPDATE_READY:
      return state.merge({
        isUpdateReady: true,
      });
    default:
      return state;
  }
}

let updateTimerInterval;

export function displayUpdateProgress(dispatch) {
  function onUpdateReady() {
    clearInterval(updateTimerInterval);
    dispatch(updateReady());
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
