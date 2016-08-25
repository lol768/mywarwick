import $ from 'jquery';
import 'jquery-form/jquery.form.js';

const SPLIT_FORM = 'form.split-form';

$(SPLIT_FORM).each((i, form) => {
  const $form = $(form);

  function getHashNum() {
    return parseInt(location.hash.substring(1), 10);
  }

  let currentPage = getHashNum() || 0;

  function showSection(num) {
    history.replaceState({ pageNum: num }, location.href);
    currentPage = num;
    const $section = $form.find(`section:eq(${num})`);
    $form.find('section.active').removeClass('active').hide();
    $section.show().addClass('active');
  }

  function pushSection(num) {
    history.pushState({ pageNum: num }, location.href);
    showSection(num);
  }

  $form.find('section:not(:last)')
    .append('<button class="btn btn-primary next pull-right">Next</button>');
  $form.find('section:not(:first)')
    .append('<button type="button" class="btn btn-default back pull-left">Back</button>');

  showSection(currentPage);

  function validate() {
    const formAction = $form.attr('action');

    return new Promise((resolve, reject) =>
      $form.ajaxSubmit({
        url: `${formAction}/validate`,
        success: resolve,
        error: reject,
        resetForm: false,
      })
    );
  }

  function replaceErrors(errs) {
    $.map(errs, err => $(`#${err.id}`).replaceWith(err));
  }

  function checkFormErrors(html) {
    return new Promise((res, rej) => {
      const errors = $(html).find(`section:eq(${currentPage}) .has-error`);
      if (errors.length > 0) {
        rej(errors);
      } else {
        res();
      }
    });
  }

  $('button.next').on('click', function onClick(e) {
    e.preventDefault();

    const $button = $(this).prop('disabled', true);

    validate()
      .then(html => checkFormErrors(html)
        .then(() => {
          $form.find(`section:eq(${currentPage}) > *`).removeClass('has-error');
          return pushSection(currentPage + 1);
        })
        .catch(replaceErrors)
      )
      .catch(() => {}) // TODO: log ting?
      .then(() => $button.prop('disabled', false));
  });

  window.onpopstate = e => {
    if (e.state !== null) {
      showSection(e.state.pageNum);
    }
  };

  $('button.back').on('click', () => {
    pushSection(currentPage - 1);
  });

  $form.on('submit', () => {
    validate()
      .then(html =>
        checkFormErrors(html)
          .then(() => $form.off().submit())
          .catch(replaceErrors)
      ).catch(() => {}); // TODO: log ting?
    return false;
  });
});
