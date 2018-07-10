var $container = jQuery('.report-content');
var start = $container.data('start') || '';
var end = $container.data('end') || '';
var cacheTimeout = $container.data('cachelife') || 'a short while';
var pollTimeoutInMins = 10;
var pollTimeoutInMillis = 60000 * pollTimeoutInMins;
var pollIntervalInMillis = 1500;

function sleep(ms) {
  return new Promise(function(resolve) {
    setTimeout(resolve, ms);
  });
}

function poll() {
  var endTime = Number(new Date()) + pollTimeoutInMillis;
  
  return fetch('/admin/reports/clients/' + start + '/' + end, {
    credentials: 'include'
  })
    .then(function(response) {
      if (response.status === 200) {
        return response.text();
      } else if (Number(new Date()) > endTime) {
        reject(new Error('timeout'));
      } else {
        return sleep(pollIntervalInMillis);
      }
    })
    .then(function(result) {
      if (typeof result == 'string') {
        $container.html(result);
      } else {
        return poll();
      }
    })
    .catch(function() {
      $container.html('<p class="alert alert-warning">The report has not been built after ' + pollTimeoutInMins + ' minutes. Please <i>Search</i> again later to see if the report is available. Reports are cached for ' + cacheTimeout + '.</p>');
    });
}

poll();
