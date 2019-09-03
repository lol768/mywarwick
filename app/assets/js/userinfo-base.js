/* eslint-env browser */

import querystring from 'querystring';
import url from 'url';
import * as serverpipe from './serverpipe';

/**
 * A minimal set of methods for fetching the current user before
 * we load the rest of the app. No dependencies on React/Redux.
 */

// kicks off the whole data flow - when user is received we fetch tile data
export function fetchUserInfo() {
  return serverpipe.fetchWithCredentials('/user/info');
}

export function rewriteRefreshUrl(location, currentLocation) {
  const parsed = url.parse(location, true);
  parsed.query.target = currentLocation;
  parsed.query.myWarwickRefresh = true;
  parsed.search = querystring.stringify(parsed.query);
  return url.format(parsed);
}

export function handleRedirects(response) {
  const json = response.json ? response.json() : Promise.resolve(response);
  return json.then((data) => {
    if (data.refresh) {
      window.location = rewriteRefreshUrl(data.refresh, window.location.href);
      return [data, true];
    }
    if (!data.user.authenticated) {
      window.location = data.links.login;
      return [data, true];
    }
    return [data, false];
  });
}
