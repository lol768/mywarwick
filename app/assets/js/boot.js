import './polyfills-and-errors';
import log from 'loglevel';
import * as u from './userinfo-base';
import { setMethod } from './csrfToken';


log.enableAll(false);
log.debug(`Environment: ${process.env.NODE_ENV}`);

function boot() {
  u.fetchUserInfo()
    .then(u.handleRedirects)
    .catch(() => [null, false])
    .then(([userData, handled]) => {
      if (!handled) {
        if (userData !== null) {
          setMethod('userInfo', userData);
        }
        return import('./main').then(main =>
          main.launch(userData)
        );
      }
      // we are redirecting to SSO, so do nothing else.
      return null;
    });
}

boot();
