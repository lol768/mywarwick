import $ from 'jquery';
import 'jquery-form/jquery.form.js';
import Promise from 'bluebird';

export function promiseSubmit(form, options = {}) {
  return new Promise((resolve, reject) =>
    $(form).ajaxSubmit({
      ...options,
      success: resolve,
      error: reject,
      resetForm: false,
    })
  );
}
