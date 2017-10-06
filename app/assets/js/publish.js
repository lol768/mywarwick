/* eslint-env browser */

import React from 'react';
import ReactDOM from 'react-dom';
import $ from 'jquery';
import FileUpload from './publish/components/FileUpload';
import { fetchWithCredentials } from './serverpipe';
import './publish/news';
import './publish/groupPicker';
import './publish/modulePicker';
import _ from 'lodash-es';
import './flexi-picker';
import AudiencePicker from './publish/components/AudiencePicker';
import store from './publish/publishStore';


function setupAudiencePicker() {
  const audiencePicker = $('.audience-picker');

  if (audiencePicker.length) {
    const props = {
      departments: audiencePicker.data('departments') || [],
      isGod: audiencePicker.data('is-god') || false,
      formData: audiencePicker.data('form-data') || {},
      locationOpts: audiencePicker.data('location-opts') || {},
      deptSubsetOpts: audiencePicker.data('dept-subset-opts') || {},
      store,
    };
    ReactDOM.render(
      <AudiencePicker {...props} />,
      audiencePicker.get(0),
    );
  }
}

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
    <FileUpload inputName={inputName.value} imageId={imageId} />,
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
          .append(document.createTextNode(`${$option.text()} (${$option.val()}) `))
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

function setupPublisherPermissionsForm() {
  $('.edit-publisher-permissions').each((i, form) => {
    const $form = $(form);
    const $permissionsContainer = $form.find('.permissions');

    function updateIndexes() {
      $permissionsContainer.find('.form-control-static').each((index, item) => {
        $(item)
          .find('input[name$=".usercode"]')
          .prop('name', `permissions[${index}].usercode`)
          .end()
          .find('input[name$=".role"]')
          .prop('name', `permissions[${index}].role`);
      });
    }

    $permissionsContainer.on('click', '.btn-danger', (e) => {
      $(e.target).closest('p').remove();
      updateIndexes();
    });

    const $addButton = $form.find('.add-permission .btn.btn-default').on('click', () => {
      const userData = $form.find('.add-permission input[name=usercode]').data('item');
      const $option = $form.find('.add-permission select option:selected');
      const roleName = $option.text();
      const roleType = $option.val();
      $permissionsContainer.append(
        $('<p/>')
          .addClass('form-control-static')
          .append(document.createTextNode(`${userData.title} (${userData.value}) : ${roleName} `))
          .append(
            $('<button/>').prop('type', 'button').addClass('btn btn-danger btn-xs').html('Remove'),
          )
          .append(
            $('<input/>').prop({
              type: 'hidden',
              name: 'permissions[].usercode',
              value: userData.value,
            }),
          )
          .append(
            $('<input/>').prop({
              type: 'hidden',
              name: 'permissions[].role',
              value: roleType,
            }),
          ),
      );
      updateIndexes();
    }).prop('disabled', true);
    $form.find('.add-permission input[name=usercode]')
      .on('richResultField.store', () => {
        $addButton.prop('disabled', false);
      })
      .on('richResultField.edit', () => {
        $addButton.prop('disabled', true);
      });
  });
}

$(() => {
  setupAudiencePicker();
  setupPublisherDepartmentsForm();
  setupPublisherPermissionsForm();

  $('[data-background-color]').each(function applyBackgroundColour() {
    $(this).css('background-color', $(this).data('background-color'));
  });

  // Init popovers
  $(document).popover({
    selector: '.toggle-popover',
    container: '.id7-main-content-area',
    trigger: 'click',
  });

  // Dismiss popover on document click
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

  $('.activity-item__send-progress').each(function watchSendStatus() {
    const $activity = $(this).parents('.activity-item');
    const activityId = $activity.data('activity-id');

    const interval = setInterval(() => {
      fetchWithCredentials(`alerts/${activityId}/status`)
        .then(response => response.text())
        .then(text => JSON.parse(text))
        .then((response) => {
          if (response.sendingNow) {
            $activity.find('.activity-item__sent-count').text(response.sentCount);
          } else {
            clearInterval(interval);
            $activity.find('.activity-item__send-progress').remove();

            if ($('.activity-item__send-progress').length === 0) {
              $('#sending-empty').removeClass('hidden');
            }

            $activity.prependTo('#sent-activities');
          }
        });
    }, 2000);
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
