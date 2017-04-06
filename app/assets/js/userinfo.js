import * as serverpipe from './serverpipe';
import store from './store';
import * as user from './state/user';
import * as analytics from './analytics';

// kicks off the whole data flow - when user is received we fetch tile data
export function fetchUserInfo() {
  return serverpipe.fetchWithCredentials('/user/info');
}

export function receiveUserInfo(response) {
  return response.json()
    .then(data => {
      if (data.refresh) {
        window.location = user.rewriteRefreshUrl(data.refresh, window.location.href);
      } else {
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
