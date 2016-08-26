import $ from 'jquery';
import _ from 'lodash';
import { promiseSubmit } from './utils';
import log from 'loglevel';

$('.split-form').each((i, form) => {
  const $form = $(form);

  const $audienceEstimateContainer = $form.find('.audience-estimate-container');
  const $audienceEstimate = $audienceEstimateContainer.find('.audience-estimate-value');

  $audienceEstimateContainer.hide();

  function updateAudienceEstimate() {
    const timeout = setTimeout(() => {
      $audienceEstimate.html('<i class="fa fa-spin fa-refresh"></i>');
    }, 300);

    promiseSubmit($form, {
      url: $form.attr('data-audience-action'),
      dataType: 'json',
    })
      .then(response => {
        const number = response.data;
        const people = number === 1 ? 'person' : 'people';

        $audienceEstimate.text(number < 0 ? 'public' : `${number.toLocaleString()} ${people}`);
        $audienceEstimateContainer.show();
      })
      .catch(e => {
        log.error('Audience estimate returned error', e);
        $audienceEstimateContainer.hide();
      })
      .then(() => clearTimeout(timeout));
  }

  if ($audienceEstimate.length > 0) {
    $form.on(
      'change',
      [
        '.audience-picker :input',
        '#item_category_field :input',
        '#item_ignoreCategories_field :input',
      ].join(', '),
      // Defer so any changes to other fields made as a result of this change
      // propagate before requesting the audience size estimate.  Debounce
      // to place a rate limit on requests.
      () => _.defer(_.debounce(updateAudienceEstimate, 500))
    );
  }
});
