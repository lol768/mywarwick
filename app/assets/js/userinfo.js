/* global MyWarwickNative */
/* eslint-env browser */

import $ from 'jquery';
import store from './store';
import * as user from './state/user';
import * as analytics from './analytics';
import { fetchUserInfo, handleRedirects } from './userinfo-base';
import { hasAuthoritativeAuthenticatedUser } from './state';

export { fetchUserInfo } from './userinfo-base';

export function receiveUserInfo(response) {
  return handleRedirects(response)
    .then(([data, handled]) => {
      if (!handled) {
        store.dispatch(user.receiveSSOLinks(data.links));

        const analyticsData = data.user.analytics;
        if (analyticsData !== undefined) {
          analyticsData.dimensions.forEach(dimension =>
            analytics.setDimension(dimension.index, dimension.value),
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
    .catch((e) => {
      setTimeout(() => fetchUserInfo().then(receiveUserInfo), 5000);
      throw e;
    });
}

const feedbackFormLocation =
  'http://warwick.ac.uk/my/feedback';

export function showFeedbackForm(deviceDetails) {
  const state = store.getState();
  let userDetails = {};
  if (state !== undefined && hasAuthoritativeAuthenticatedUser(state)) {
    userDetails = {
      usercode: state.user.data.usercode,
      name: state.user.data.name,
    };
  }
  window.location = `${feedbackFormLocation}?${$.param(Object.assign(deviceDetails, userDetails))}`;
}

export function loadDeviceDetails() {
  if (typeof MyWarwickNative !== 'undefined' && MyWarwickNative.loadDeviceDetails) {
    MyWarwickNative.loadDeviceDetails();
  } else {
    showFeedbackForm({
      os: navigator.platform,
      model: navigator.userAgent,
      'screen-width': $(window).width(),
      'screen-height': $(window).height(),
      path: window.location.pathname,
    });
  }
}

export function signOut() {
  const state = store.getState();
  window.location = state.user.links.logout;
}

