import $ from 'jquery';
import 'bootstrap-3-typeahead';
import log from 'loglevel';
import { postJsonWithCredentials } from '../serverpipe';

class ModulePicker {
  constructor(input, {
    addItem = () => {},
  }) {
    const $element = $(input);

    // Disable browser autocomplete dropdowns, it gets in the way.
    $element.attr('autocomplete', 'off');

    this.addItem = addItem;

    let currentQuery = null;

    $element.typeahead({
      source: (query, callback) => {
        currentQuery = query;
        postJsonWithCredentials('/service/grouplookup/module', { query })
          .then(response => response.json())
          .catch((e) => {
            log.error(e);
            return [];
          })
          .then((response) => {
            // Return the items only if the user hasn't since made a different query
            if (currentQuery === query) {
              callback(response.modules || []);
            }
          });
      },
      highlighter: (html, item) => (`<strong>${item.code.toUpperCase()}</strong>: ${item.name}<br><em>${item.departmentName}</em>`),
      delay: 600,
      matcher: () => true, // All data received from the server matches the query
      afterSelect: (item) => {
        const text = `${item.code.toUpperCase()}: ${item.name}`;
        this.addItem({ value: item.code.toUpperCase(), text });
        $element.data('item', item);
        $element.val(''); // return to placeholder text
      },
    });
  }
}

export default function modulePicker(options = {}) {
  const $this = $(this);
  if ($this.data('module-picker')) {
    throw new Error('ModulePicker has already been added to this element');
  }
  $this.data('module-picker', new ModulePicker(this, options));
}
