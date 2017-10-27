import $ from 'jquery';
import { postJsonWithCredentials } from '../serverpipe';
import 'bootstrap-3-typeahead';
import log from 'loglevel';
import _ from 'lodash-es';

class RelationshipPicker {
  constructor(input, {
    addItem = () => {
    },
  }) {
    const $element = $(input);

    // Disable browser autocomplete dropdowns, it gets in the way.
    $element.attr('autocomplete', 'off');

    this.addItem = addItem;

    let currentQuery = null;

    $element.typeahead({
      source: (query, callback) => {
        currentQuery = query;
        postJsonWithCredentials('/service/flexipicker', {
          includeUsers: true,
          universityId: true,
          query,
        }).then(response => response.json())
          .catch((e) => {
            log.error(e);
            return [];
          })
          .then((response) => {
            // Return the items only if the user hasn't since made a different query
            if (currentQuery === query && currentQuery !== '') {
              const staff = _.filter(response.data.results, o => o.isStaff);
              callback(staff || []);
            }
          });
      },
      highlighter: (html, item) => (`<strong>${item.name}</strong><br><em>${item.department}</em>`),
      delay: 600,
      matcher: () => true, // All data received from the server matches the query
      afterSelect: (item) => {
        postJsonWithCredentials('/service/grouplookup/relationships', { query: item.value })
          .then(response => response.json())
          .catch((e) => {
            log.error(e);
            return [];
          })
          .then((response) => {
            const relationships = response.relationships;
            const text = `${item.name} (${item.department})`;
            this.addItem({
              value: item.value,
              text,
              options: relationships.map((obj) => {
                const keys = Object.keys(obj);
                return keys.map(key => ({
                  [key]: { ...obj[key], selected: false },
                }));
              }).reduce((a, b) => a.concat(b), []),
            });
            $element.data('item', item);
            $element.val(''); // return to placeholder text
          });
      },
    });
  }
}

export default function relationshipPicker(options = {}) {
  const $this = $(this);
  if ($this.data('relationship-picker')) {
    throw new Error('RelationshipPicker has already been added to this element.');
  }
  $this.data('relationship-picker', new RelationshipPicker(this, options));
}
