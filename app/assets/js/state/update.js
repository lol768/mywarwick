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

export function displayUpdateProgress(dispatch) {
  function onUpdateReady() {
    dispatch(updateReady());
  }

  if ('applicationCache' in window && !('serviceWorker' in navigator)) {
    window.applicationCache.addEventListener('updateready', onUpdateReady);

    if (window.applicationCache.status === window.applicationCache.UPDATEREADY) {
      onUpdateReady();
    }
  }
}
