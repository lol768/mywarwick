import $ from 'jquery';

/*
  Attempt to register service worker - we don't do notifications or offline but it's nice to keep it
  up to date.
*/
if ('serviceWorker' in navigator) {
  navigator.serviceWorker.register('/service-worker.js');
}


$(() => {
  // Audience pickers
  $('.audience-picker').each((i, el) => {
    const $el = $(el);
    const $deptBoxes = $(el).find('input[value*="Dept:"]');
    const $deptSelect = $el.find('[name=department]');

    const $publicBox = $(el).find('input[value=Public]');
    const $otherInputs = $(el).find('input, select').not($publicBox);
    $publicBox.on('change', () => {
      if ($publicBox.is(':checked')) {
        $otherInputs.attr('disabled', true);
      } else {
        $otherInputs.removeAttr('disabled');
        $deptSelect.trigger('change');
      }
    });

    $deptSelect.on('change', e => {
      const deptSelected = e.target.value;
      const $subsets = $deptBoxes.closest('.checkbox');
      $deptBoxes.attr('disabled', !deptSelected);
      $subsets.toggleClass('disabled', !deptSelected);
      if (!deptSelected) {
        $subsets.find('input').prop('checked', false);
      }
    }).trigger('change');
  });
});
