import $ from 'jquery';

$('input[name="item.publishDateSet"]').on('change', () => {
  const showDateField = $('#item_publishDateSet_true').is(':checked');

  $('#item_publishDate_field').toggle(showDateField);
}).trigger('change');
