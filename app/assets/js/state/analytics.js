const initialState = {
  clientId: null
};

export function reducer(state = initialState, action) {
  switch (action.type) {
    case 'analytics.client-id':
      return {
        ...state,
        clientId: action.payload,
      };
    default:
      return state;
  }
}
