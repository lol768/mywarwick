import $ from 'jquery';

$('input[name="item.publishDateSet"]').on('change', function onChange() {
  const showDateField = $(this).filter(':checked').val() === 'true';

  $(this).parents('.form-group').next().toggle(showDateField);
}).trigger('change');
