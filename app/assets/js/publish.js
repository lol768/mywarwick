/* eslint-env browser */

import React from 'react';
import ReactDOM from 'react-dom';
import $ from 'jquery';
import FileUpload from './publish/components/FileUpload';
import './publish/news';
import './publish/groupPicker';
import _ from 'lodash-es';

/*
 Attempt to register service worker - we don't do notifications or offline but it's nice to keep it
 up to date.
 */
if ('serviceWorker' in navigator) {
  navigator.serviceWorker.register('/service-worker.js');
}

const fileUploadContainer = document.getElementById('file-upload-container');

if (fileUploadContainer) {
  const inputName = fileUploadContainer.attributes['data-input-name'];
  const imageId = fileUploadContainer.attributes['data-image-id'].value;

  ReactDOM.render(
    <FileUpload inputName={ inputName.value } imageId={ imageId } />,
    fileUploadContainer,
  );
}

function setupPublisherDepartmentsForm() {
  function toggleAllDepartments($checkbox, $form) {
    if ($checkbox.is(':checked')) {
      $form.find('.departments .btn').prop('disabled', true);
      $form.find('.add-department').find('select, .btn').prop('disabled', true);
    } else {
      $form.find('.departments .btn').prop('disabled', false);
      $form.find('.add-department').find('select, .btn').prop('disabled', false);
    }
  }

  $('.edit-publisher-departments').each((i, form) => {
    const $form = $(form);

    const $departmentsContainer = $form.find('.departments');
    $departmentsContainer.on('click', '.btn-danger', (e) => {
      $(e.target).closest('p').remove();
    });

    $form.find('.add-department .btn').on('click', (e) => {
      const $option = $(e.target).closest('div').find('select option:selected');
      $departmentsContainer.append(
        $('<p/>')
          .addClass('form-control-static')
          .append(`${$option.text()} (${$option.val()}) `)
          .append(
            $('<button/>').prop('type', 'button').addClass('btn btn-danger btn-xs').html('Remove'),
          )
          .append(
            $('<input/>').prop({
              type: 'hidden',
              name: 'departments[]',
              value: $option.val(),
            }),
          ),
      );
    });

    const $checkbox = $form.find('[name="isAllDepartments"]');
    $checkbox.on('change', () => {
      toggleAllDepartments($checkbox, $form);
    });
    toggleAllDepartments($checkbox, $form);
  });
}

function wireEventListeners() {
  $('.audience-picker').each((i, el) => {
    const $el = $(el);
    const $deptBoxes = $el.find('input[value*="Dept:"]');
    const $deptSelect = $el.find('[data-select=department]');

    const $publicBox = $el.find('input[value=Public]');
    const $otherInputs = $el.add($('.audience-picker-extra')).find('input, select').not($publicBox);
    $publicBox.on('change', () => {
      if ($publicBox.is(':checked')) {
        $otherInputs.attr('disabled', true);
      } else {
        $otherInputs.removeAttr('disabled');
        $deptSelect.trigger('change');
      }

      $deptSelect.trigger('change');
    }).trigger('change');

    $deptSelect.on('change', (e) => {
      const deptSelected = e.target.value && !$deptSelect.is(':disabled');
      const $subsets = $deptBoxes.closest('.checkbox');
      $deptBoxes.attr('disabled', !deptSelected);
      $subsets.toggleClass('disabled', !deptSelected);
    }).trigger('change');
  });
}

$(() => {
  wireEventListeners();
  setupPublisherDepartmentsForm();

  $('[data-background-color]').each(function applyBackgroundColour() {
    $(this).css('background-color', $(this).data('background-color'));
  });

  $(document).popover({
    selector: '.toggle-popover',
    container: '.id7-main-content-area',
    trigger: 'click',
  });

  $(document).on('click', (e) => {
    const $target = $(e.target);
    if ($target.hasClass('toggle-popover') || $target.closest('.popover').length > 0) {
      if (!$target.hasClass('popover-active')) {
        $target.toggleClass('popover-active').popover('toggle');
      }
    } else {
      // click elsewhere on body, dismiss all open popover
      $('.popover-active').popover('hide').removeClass('popover-active');
    }
  });

  $('.fa-picker-preview').each((i, input) => {
    const $input = $(input);
    const debouncedPicker = _.debounce(() => {
      $input
        .closest('div.input-group')
        .find('.input-group-addon i')
        .prop('class', `fa fa-fw fa-${$input.val()}`);
    }, 500);
    $input.on('keydown', debouncedPicker);
  });
});
