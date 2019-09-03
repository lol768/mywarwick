/* eslint-env browser */

import React from 'react';
import ReactDOM from 'react-dom';
import $ from 'jquery';
import _ from 'lodash-es';
import { Provider } from 'react-redux';
import Tablesort from 'tablesort';
import log from 'loglevel';

import AudiencePicker from './publish/components/AudiencePicker';
import AudienceIndicator from './publish/components/AudienceIndicator';
import store from './publish/publishStore';
import promiseSubmit from './publish/utils';
import NewsCategoryPicker from './publish/components/NewsCategoryPicker';
import { fetchWithCredentials } from './serverpipe';
import FileUpload from './publish/components/FileUpload';

import './flexi-picker';
import './publish/news';
import './publish/groupPicker';
import './publish/modulePicker';

function tablesortAddNumberSorting() {
  Tablesort.extend('number',
    item => item.match(/^[-+]?(\d)*-?([,.]){0,1}-?(\d)+([E,e][-+][\d]+)?%?$/),
    (a, b) => parseInt(a, 10) - parseInt(b, 10));
}

function setupAudienceIndicator() {
  const audienceIndicator = $('.audience-indicator');
  const hint = audienceIndicator.data('hint');
  const props = {
    promiseSubmit,
    hint,
  };
  if (audienceIndicator.length) {
    setTimeout(() => {
      ReactDOM.render(
        <Provider store={store}>
          <AudienceIndicator
            {...props}
          />
        </Provider>,
        audienceIndicator.get(0),
      );
    }, 200);
  }
}

function setupAudiencePicker() {
  const audiencePicker = $('.audience-picker');

  if (audiencePicker.length) {
    const props = {
      departments: audiencePicker.data('departments') || [],
      isGod: audiencePicker.data('is-god') || false,
      formData: audiencePicker.data('form-data') || {},
      locationOpts: audiencePicker.data('location-opts') || {},
      deptSubsetOpts: audiencePicker.data('dept-subset-opts') || {},
      hallsOfResidence: audiencePicker.data('halls-of-residence') || {},
      store,
    };
    ReactDOM.render(
      <Provider store={store}>
        <AudiencePicker {...props} />
      </Provider>,
      audiencePicker.get(0),
    );
  }
}

function setupCategoryPicker() {
  const categoryPicker = $('.category-picker');
  if (categoryPicker.length) {
    const props = {
      newsCategories: categoryPicker.data('categories') || {},
      formData: categoryPicker.data('form-data') || {},
      store,
    };
    ReactDOM.render(
      <Provider store={store}>
        <NewsCategoryPicker {...props} />
      </Provider>,
      categoryPicker.get(0),
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

function fetchActivityStatus(activityId) {
  return fetchWithCredentials(`alerts/${activityId}/status`)
    .then(response => response.text())
    .then(text => JSON.parse(text));
}

function initSentDetails() {
  const $sentActivityItems = $('.activity-item__audience[data-sent=true]');
  $sentActivityItems.find('.activity-item__messages-*').show();

  $sentActivityItems.each((i, el) => {
    const $item = $(el);
    const activityId = $item.parents('.activity-item').data('activity-id');
    fetchActivityStatus(activityId)
      .then(({ sent: { delivered, readCount } }) => {
        $item.find('.activity-item__messages-read-val').text(readCount);
        if (typeof delivered === 'undefined' && typeof readCount === 'undefined') {
          $item.html('<div class="col-sm-12"><i class="fa fa-exclamation-triangle"></i> Error fetching sent details for this alert</div>');
        } else if (typeof delivered === 'undefined') { // NEWSTART-1240 old alerts won't have this data
          $item.find('[class^=activity-item__messages-delivered-]').hide();
        } else {
          $item.find('.activity-item__messages-delivered-val').text(delivered);
        }
      })
      .catch((err) => {
        $item.html('<div class="col-sm-12"><i class="fa fa-exclamation-triangle"></i> Error fetching sent details for this alert</div>');
        log.error(`Error updating alert sent details from json response. Alert Id: ${activityId}`, err);
      });
  });
}

function sortableTables() {
  $('table.sortable-table').each((i, el) => new Tablesort(el));
}

$(() => {
  setupAudienceIndicator();
  setupAudiencePicker();
  setupPublisherDepartmentsForm();
  setupPublisherPermissionsForm();
  setupCategoryPicker();
  initSentDetails();
  tablesortAddNumberSorting();
  sortableTables();

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
      fetchActivityStatus(activityId)
        .then((response) => {
          if (response.sendingNow) {
            $activity.find('.activity-item__sent-count').text(response.sent.total);
          } else {
            clearInterval(interval);
            $activity.find('.activity-item__send-progress').remove();

            if ($('.activity-item__send-progress').length === 0) {
              $('#sending-empty').removeClass('hidden');
            }

            $activity.prependTo('#sent-activities');
            $activity.find('.activity-item__audience').attr('data-sent', true);
            initSentDetails();
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
        .prop('class', `fal fa-fw fa-${$input.val()}`);
    }, 500);
    $input.on('keydown', debouncedPicker);
  });

  $('#item_provider').each((i, select) => {
    const $select = $(select);
    const data = $select.data('overrideMuting');
    const $overrideMutingInfo = $select.closest('.form-group').next('.form-group');
    function update() {
      if (data && data[$select.val()]) {
        $overrideMutingInfo.removeClass('hidden');
      } else {
        $overrideMutingInfo.addClass('hidden');
      }
    }
    $select.on('change', update);
    update();
  });
});
