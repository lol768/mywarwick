import { registerReducer } from './reducers';

export const NAVIGATE = 'path.navigate';

export function navigate(path) {
  return {
    type: NAVIGATE,
    path: path
  };
}

registerReducer('path', (state = '/', action) => {
  switch (action.type) {
    case NAVIGATE:
      return action.path;
    default:
      return state;
  }
});
