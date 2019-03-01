import $ from 'jquery';
import 'eonasdan-bootstrap-datetimepicker';
import moment from 'moment';

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

const dateTimeHiddenFieldFormat = 'YYYY-MM-DD[T]HH:mm:ss';
const dateTimeTextFieldFormat = 'Do MMM YYYY, HH:mm';

$('.datetimepicker').each(function setUpDateTimePicker() {
  const hiddenField = $(this).find('input[type=hidden]');
  const inputGroup = $(this).find('.input-group');
  const textField = inputGroup.find('input');

  textField.val(
    moment(hiddenField.val(), dateTimeHiddenFieldFormat).format(dateTimeTextFieldFormat),
  );

  inputGroup.datetimepicker({
    format: dateTimeTextFieldFormat,
    icons,
    sideBySide: true,
    allowInputToggle: true,
    stepping: 1,
  }).on('dp.change', ({ date }) =>
    hiddenField.val(moment(date, dateTimeTextFieldFormat).format(dateTimeHiddenFieldFormat)),
  );
});

const dateHiddenFieldFormat = 'YYYY-MM-DD';
const dateTextFieldFormat = 'Do MMM YYYY';

$('.datepicker').each(function setUpDatePicker() {
  const hiddenField = $(this).find('input[type=hidden]');
  const inputGroup = $(this).find('.input-group');
  const textField = inputGroup.find('input');

  textField.val(moment(hiddenField.val(), dateHiddenFieldFormat).format(dateTextFieldFormat));

  inputGroup.datetimepicker({
    format: dateTextFieldFormat,
    icons,
    sideBySide: true,
    allowInputToggle: true,
    stepping: 1,
  }).on('dp.change', ({ date }) =>
    hiddenField.val(moment(date, dateTextFieldFormat).format(dateHiddenFieldFormat)),
  );
});
