/* eslint-env browser */

// In case of JS error, hide the loading spinner.
// Has to be up top to catch all possible JS errors.
if (window.addEventListener) {
  window.myWarwickErrorHandler = () => {
    const spinner = document.getElementById('react-app-spinner');
    if (spinner) spinner.style.display = 'none';

    const message = document.getElementById('error-fallback');
    if (message) message.style.display = 'block';
  };
  window.addEventListener('error', window.myWarwickErrorHandler, true);
}

import 'core-js/modules/es.object.assign';
import 'core-js/modules/es.array.includes';
import promisePolyfill from 'es6-promise/lib/es6-promise/polyfill';

promisePolyfill();

import initErrorReporter from './errorreporter';

initErrorReporter();
