import $ from 'jquery';
import _ from 'lodash-es';
import React from 'react';
import ReactDOM from 'react-dom';
import log from 'loglevel';
import AudienceIndicator from './components/AudienceIndicator';
import promiseSubmit from './utils';

$('.split-form').each((i, form) => {
  const $form = $(form);

  const $audienceIndicator = $form.find('.audience-indicator');
  const container = $audienceIndicator[0];

  function updateAudienceEstimate() {
    const timeout = setTimeout(() => {
      ReactDOM.render(
        <AudienceIndicator fetching />,
        container,
      );
    }, 200);

    promiseSubmit($form, {
      url: $form.attr('data-audience-action'),
      dataType: 'json',
    })
      .then((response) => {
        ReactDOM.render(
          <AudienceIndicator {...response.data} />,
          container,
        );
      })
      .catch((e) => {
        log.error('Audience estimate returned error', e);
        ReactDOM.render(
          <AudienceIndicator error />,
          container,
        );
      })
      .then(() => clearTimeout(timeout));
  }

  if (container !== undefined) {
    ReactDOM.render(
      <AudienceIndicator empty />,
      container,
    );

    $form.on(
      'change',
      [
        '.audience-picker :input',
        '#item_category_field :input',
        '#item_ignoreCategories_field :input',
        '.audience-picker-extra :input',
      ].join(', '),
      // Defer so any changes to other fields made as a result of this change
      // propagate before requesting the audience size estimate.  Debounce
      // to place a rate limit on requests.
      () => _.defer(_.debounce(updateAudienceEstimate, 500)),
    );
  }

  $('[data-toggle="tooltip"]').tooltip();
});
