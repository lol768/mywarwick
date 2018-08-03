/* eslint-env browser */

import $ from 'jquery';

export const API_BASE = '/api/news/images';

const errorMessages = {
  timeout: 'The request timed out',
  error: 'Unable to contact the server',
  abort: 'The request was aborted',
  parsererror: 'The server response was invalid',
};

export function put(file, progressCallback) {
  const data = new FormData();
  data.append('image', file);

  return new Promise((resolve, reject) => {
    $.ajax({
      url: API_BASE,
      type: 'post',
      data,
      processData: false,
      contentType: false,
      dataType: 'json',
      mimeType: 'multipart/form-data',
      success: (json) => {
        if (json.success) {
          resolve(json.data);
        } else {
          // Wouldn't expect to be here, but need to ensure we resolve or reject
          const errMsg = (json.errors && json.errors.length) ?
            json.errors[0].message : 'file upload was unsuccessful';
          reject(new Error(errMsg));
        }
      },
      error: (e, errorType, statusText) => {
        if (e.responseJSON.errors && e.responseJSON.errors.length) {
          reject(new Error(e.responseJSON.errors[0].message));
        } else {
          reject(new Error(statusText || errorMessages[errorType || 'error']));
        }
      },
      xhr: () => {
        const xhr = $.ajaxSettings.xhr();
        xhr.upload.addEventListener('progress', e => progressCallback(e.loaded, e.total));
        return xhr;
      },
    });
  });
}
