import thunkMiddleware from 'redux-thunk';

import { createStore, applyMiddleware } from 'redux';

import app from './reducers';

const createStoreWithMiddleware = applyMiddleware(thunkMiddleware)(createStore);

export default createStoreWithMiddleware(app);
