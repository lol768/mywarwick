import Immutable from 'immutable';

import { registerReducer } from './reducers';

const UPDATE_START = 'update.start';
const UPDATE_PROGRESS = 'update.progress';
const UPDATE_READY = 'update.ready';

function updateStart() {
  return {
    type: UPDATE_START
  };
}

function updateProgress(loaded, total) {
  return {
    type: UPDATE_PROGRESS,
    loaded: loaded,
    total: total
  };
}

function updateReady() {
  return {
    type: UPDATE_READY
  };
}

const initialState = Immutable.fromJS({
  isUpdating: false,
  loaded: 0,
  total: 0
});

registerReducer('update', (state = initialState, action) => {
  switch (action.type) {
    case UPDATE_START:
      return state.merge({
        isUpdating: true
      });
    case UPDATE_PROGRESS:
      return state.merge({
        isUpdating: true,
        loaded: action.loaded,
        total: action.total
      });
    case UPDATE_READY:
      return state.merge({
        isUpdating: true,
        loaded: state.get('total')
      });
    default:
      return state;
  }
});

export function displayUpdateProgress() {
  return dispatch => {
    if ('applicationCache' in window) {
      function onDownloading() {
        dispatch(updateStart());
      }

      function onProgress(e) {
        dispatch(updateProgress(e.loaded, e.total));
      }

      function onUpdateReady() {
        dispatch(updateReady());
      }

      window.applicationCache.addEventListener('progress', onProgress);
      window.applicationCache.addEventListener('downloading', onDownloading);
      window.applicationCache.addEventListener('updateready', onUpdateReady);

      if (window.applicationCache.status == window.applicationCache.UPDATEREADY) {
        onUpdateReady();
      }
    }
  };
}

