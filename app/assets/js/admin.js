import React from 'react';
import ReactDOM from 'react-dom';
import FileUpload from './admin/components/FileUpload';

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

  ReactDOM.render(
    <FileUpload inputName={ inputName.value } />,
    fileUploadContainer
  );
}
