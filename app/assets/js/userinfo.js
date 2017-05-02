import store from './store';
import * as user from './state/user';
import * as analytics from './analytics';

import { fetchUserInfo, handleRedirects } from './userinfo-base';
export { fetchUserInfo } from './userinfo-base';

export function receiveUserInfo(response) {
  return handleRedirects(response)
    .then(([data, handled]) => {
      if (!handled) {
        store.dispatch(user.receiveSSOLinks(data.links));

        const analyticsData = data.user.analytics;
        if (analyticsData !== undefined) {
          analyticsData.dimensions.forEach(dimension =>
            analytics.setDimension(dimension.index, dimension.value)
          );

          analytics.setUserId(analyticsData.identifier);
        }

        analytics.ready();

        store.dispatch(user.userReceive(data.user)).then(() => {
          if (!data.user.authenticated) {
            window.location = data.links.login;
          }
        });
      }
    })
    .catch(e => {
      setTimeout(() => fetchUserInfo().then(receiveUserInfo), 5000);
      throw e;
    });
}
