import $ from 'jquery';
import 'jquery-form/jquery.form';
import '../polyfills-and-errors';

export default function promiseSubmit(form, options = {}) {
  return new Promise((resolve, reject) => $(form).ajaxSubmit({
    ...options,
    success: resolve,
    error: reject,
    resetForm: false,
  }));
}
