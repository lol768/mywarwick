import $ from 'jquery';
import { postJsonWithCredentials } from '../serverpipe';
import 'bootstrap-3-typeahead';
import log from 'loglevel';

class SeminarGroupPicker {
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
        postJsonWithCredentials('/service/grouplookup/seminargroup', { query })
          .then(response => response.json())
          .catch((e) => {
            log.error(e);
            return [];
          })
          .then((response) => {
            // Return the items only if the user hasn't since made a different query
            if (currentQuery === query) {
              callback(response.seminarGroups || []);
            }
          });
      },
      highlighter: (html, item) => (
        `<strong>${item.name}</strong>: ${item.groupSetName}<br><em>${item.moduleCode}</em>`
      ),
      delay: 120,
      matcher: () => true, // All data received from the server matches the query
      afterSelect: (item) => {
        const text = `${item.name}: ${item.groupSetName}`;
        this.addItem({ value: item.id, text });
        $element.data('item', item);
        $element.val(''); // return to placeholder text
      },
    });
  }
}

export default function seminarGroupPicker(options = {}) {
  const $this = $(this);
  if ($this.data('seminar-group-picker')) {
    throw new Error('SeminarGroupPicker has already been added to this element.');
  }
  $this.data('seminar-group-picker', new SeminarGroupPicker(this, options));
}
