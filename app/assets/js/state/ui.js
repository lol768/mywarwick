import Immutable from 'immutable';
import log from 'loglevel';

let mq;
try {
  mq = require('modernizr').mq;
} catch (e) {
  log.warn('modernizr not present, using fallback.');
  mq = () => global.mqResult;
}

const isDesktop = () => mq('only all and (min-width: 768px)');
const isFourColumnLayout = () => mq('only all and (min-width: 992px)');

const initialState = Immutable.fromJS({
  className: undefined,
  isFourColumnLayout: false,
  colourTheme: 'default',
});

export function reducer(state = initialState, action) {
  switch (action.type) {
    case 'ui.class':
      return state.set('className', action.className);
    case 'ui.layout':
      return state.set('isFourColumnLayout', action.isFourColumnLayout);
    case 'ui.theme':
      return state.set('colourTheme', action.theme);
    default:
      return state;
  }
}

export function updateColourTheme(theme) {
  return {
    type: 'ui.theme',
    theme,
  };
}

export function updateUIContext() {
  return (dispatch, getState) => {
    const currentClassName = getState().getIn(['ui', 'className']);
    if (currentClassName === undefined || isDesktop() !== (currentClassName === 'desktop')) {
      dispatch({
        type: 'ui.class',
        className: isDesktop() ? 'desktop' : 'mobile',
      });
    }

    if (isFourColumnLayout() !== getState().getIn(['ui', 'isFourColumnLayout'])) {
      dispatch({
        type: 'ui.layout',
        isFourColumnLayout: isFourColumnLayout(),
      });
    }
  };
}
