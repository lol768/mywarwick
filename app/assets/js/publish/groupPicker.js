import $ from 'jquery';
import 'bootstrap-3-typeahead';
import fetch from 'isomorphic-fetch';
import log from 'loglevel';

$(() => {
  const publisherId = window.location.pathname.split('/')[2];

  let currentQuery = null;

  $('.group-picker').each(function attachPicker() {
    const $input = $(this);
    const $checkbox = $input.prev('.checkbox').find('input:checkbox');

    $input.typeahead({
      source: (query, callback) => {
        currentQuery = query;

        fetch(`/publish/${publisherId}/webgroups?query=${query}`, {
          credentials: 'same-origin',
        })
          .then(response => response.json())
          .catch(e => {
            log.error(e);
            return [];
          })
          .then(response => {
            // Return the items only if the user hasn't since made a different query
            if (currentQuery === query) {
              callback(response.groups || []);
            }
          });
      },
      highlighter: (html, item) => (item.title ?
        `<strong>${item.name}</strong><br>${item.title}` : `<strong>${item.name}</strong>`),
      delay: 200,
      matcher: () => true, // All groups received from the server match the query
      afterSelect: item => {
        if (item) {
          $checkbox
            .prop('disabled', false)
            .prop('checked', true)
            .attr('value', `WebGroup:${item.name}`);
        } else {
          $checkbox
            .prop('checked', false)
            .prop('disabled', true);
        }
      },
    });
  });
});