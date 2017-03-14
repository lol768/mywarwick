import React from 'react';
import ReactDOM from 'react-dom';
import FileUpload from './publish/components/FileUpload';
import $ from 'jquery';

import './publish/news';

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
    fileUploadContainer
  );
}

function wireEventListeners() {
  $('.audience-picker').each((i, el) => {
    const $el = $(el);
    const $deptBoxes = $(el).find('input[value*="Dept:"]');
    const $deptSelect = $el.find('[data-select=department]');

    const $publicBox = $(el).find('input[value=Public]');
    const $otherInputs = $(el).find('input, select').not($publicBox);
    $publicBox.on('change', () => {
      if ($publicBox.is(':checked')) {
        $otherInputs.attr('disabled', true);
      } else {
        $otherInputs.removeAttr('disabled');
        $deptSelect.trigger('change');
      }

      $deptSelect.trigger('change');
    }).trigger('change');

    $deptSelect.on('change', e => {
      const deptSelected = e.target.value && !$deptSelect.is(':disabled');
      const $subsets = $deptBoxes.closest('.checkbox');
      $deptBoxes.attr('disabled', !deptSelected);
      $subsets.toggleClass('disabled', !deptSelected);
    }).trigger('change');
  });
}

$(() => {
  wireEventListeners();
});
