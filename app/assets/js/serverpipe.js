

import SocketDatapipe from './SocketDatapipe';

import fetch from 'isomorphic-fetch';


//                       //
//     MESSAGE SEND      //
//                       //

export function fetchUserIdentity() {
  return () => {
    SocketDatapipe.send({
      tileId: '1',
      data: {
        type: 'who-am-i',
      },
    });
  };
}

export function fetchWithCredentials(url) {
  return fetch(url, {
    credentials: 'same-origin',
  });
}



