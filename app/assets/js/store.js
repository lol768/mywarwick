import thunk from 'redux-thunk';
import { createStore, applyMiddleware } from 'redux';
import { browserHistory } from 'react-router';
import { routerMiddleware as router } from 'react-router-redux';
import app from './reducers';

export default createStore(app,
  applyMiddleware(
    thunk,
    router(browserHistory)
  )
);
