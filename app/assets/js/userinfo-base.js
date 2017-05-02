import * as serverpipe from './serverpipe';
import querystring from 'querystring';
import url from 'url';

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
  return response.json()
    .then(data => {
      if (data.refresh) {
        window.location = rewriteRefreshUrl(data.refresh, window.location.href);
        return [data, true];
      } else if (!data.user.authenticated) {
        window.location = data.links.login;
        return [data, true];
      }
      return [data, false];
    });
}
