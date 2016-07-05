import $ from 'jquery';
import 'eonasdan-bootstrap-datetimepicker';

const icons = {
  time: 'fa fa-clock-o',
  date: 'fa fa-calendar',
  up: 'fa fa-chevron-up',
  down: 'fa fa-chevron-down',
  previous: 'fa fa-chevron-left',
  next: 'fa fa-chevron-right',
  today: 'fa fa-crosshairs',
  clear: 'fa fa-trash-o',
  close: 'fa fa-times',
};

$('.datetimepicker')
  .attr('type', 'text')
  .datetimepicker({
    format: 'YYYY-MM-DDTHH:mm',
    icons,
    sideBySide: true,
    allowInputToggle: true,
    stepping: 5,
  });

$('input[name="item.publishDateSet"]').on('change', function onChange() {
  const showDateField = $(this).filter(':checked').val() === 'true';

  $(this).parents('.form-group').next().toggle(showDateField);
}).trigger('change');
