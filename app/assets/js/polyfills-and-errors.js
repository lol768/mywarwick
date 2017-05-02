
// In case of JS error, hide the loading spinner.
// Has to be up top to catch all possible JS errors.
if (window.addEventListener) {
  window.addEventListener('error', () => {
    const spinner = document.getElementById('react-app-spinner');
    if (spinner) spinner.style.display = 'none';
  }, true);
}

import 'core-js/modules/es6.object.assign';
import promisePolyfill from 'es6-promise/lib/es6-promise/polyfill';
promisePolyfill();

import initErrorReporter from './errorreporter';
initErrorReporter();
