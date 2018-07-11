/* eslint-env browser */

import $ from 'jquery';
import fetch from 'isomorphic-fetch';
import promisePolyfill from 'es6-promise/lib/es6-promise/polyfill';

promisePolyfill();

const $clientReportContainer = $('.client-report');

function sleep(ms) {
  return new Promise((resolve) => {
    setTimeout(resolve, ms);
  });
}

function pollForClientReport() {
  const start = $clientReportContainer.data('start') || '';
  const end = $clientReportContainer.data('end') || '';
  const cacheTimeout = $clientReportContainer.data('cachelife') || 'a short while';
  const pollTimeoutInMins = 10;
  const pollTimeoutInMillis = 60000 * pollTimeoutInMins;
  const pollIntervalInMillis = 1500;
  const endTime = Number(new Date()) + pollTimeoutInMillis;

  return fetch(`/admin/reports/clients/${start}/${end}`, {
    credentials: 'same-origin',
  })
    .then((response) => {
      if (response.status === 200) {
        return response.text();
      } else if (Number(new Date()) > endTime) {
        throw new Error('timeout');
      }

      return sleep(pollIntervalInMillis);
    })
    .then((result) => {
      if (typeof result === 'string') {
        $clientReportContainer.html(result);
      } else {
        pollForClientReport();
      }
    })
    .catch(() => {
      $clientReportContainer.html(`<p class="alert alert-warning">The report has not been built after ${pollTimeoutInMins} minutes. Please <i>Search</i> again later to see if the report is available. Reports are cached for ${cacheTimeout}.</p>`);
    });
}

if ($clientReportContainer.length) {
  pollForClientReport();
}
