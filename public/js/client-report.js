var $container = jQuery('.report-content');
var start = $container.data('start') || '';
var end = $container.data('end') || '';
var cacheTimeout = $container.data('cachelife') || 'a short while';
var timeoutInMins = 10;
var timeoutInMillis = timeoutInMins * 60 * 1000;
var pollIntervalInMillis = 1500;

function poll(fn, timeout, interval) {
  var endTime = Number(new Date()) + (timeout || 2000);
  interval = interval || 100;

  var checkCondition = function(resolve, reject) {
    var ajax = fn();
    ajax.then( function(response){
      if(response.status === 200) {
        resolve(response.data);
      }
      else if (Number(new Date()) < endTime) {
        setTimeout(checkCondition, interval, resolve, reject);
      }
      else {
        reject(new Error('timed out for ' + fn + ': ' + arguments));
      }
    });
  };

  return new Promise(checkCondition);
}

poll(function() {
  return axios.get('/admin/reports/clients/' + start + '/' + end);
}, timeoutInMillis, pollIntervalInMillis).then(function(report) {
  $container.html(report);
}).catch(function() {
  $container.html('<p class="alert alert-warning">The report has not been built after ' + timeoutInMins + ' minutes. Please <i>Search</i> again later to see if the report is available. Reports are cached for ' + cacheTimeout + '.</p>')
});
