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
    history.pushState({ pageNum: num }, location.href);
    currentPage = num;
    const $section = $form.find(`section:eq(${num})`);
    $form.find('section.active').removeClass('active').hide();
    $section.show().addClass('active').fadeIn();
  }

  $form.find('section:not(:last)')
    .append('<button class="btn btn-primary next pull-right" >Next</button>');
  $form.find('section:not(:first)')
    .append('<button class="btn btn-default back pull-left" >Back</button>');

  showSection(currentPage);

  function validate() {
    return new Promise((resolve, reject) =>
      $form.ajaxSubmit({
        success: resolve,
        error: reject,
        data: { validateOnly: true },
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

  $('button.next').on('click', e => {
    e.preventDefault();
    // history.pushState({ pageNum: currentPage }, location.href);
    validate()
      .then(html => checkFormErrors(html)
        .then(() => {
          $form.find(`section:eq(${currentPage}) > *`).removeClass('has-error');
          return showSection(currentPage + 1);
        })
        .catch(replaceErrors)
      ).catch(() => {}); // TODO: log ting?
  });

  window.onpopstate = e => {
    if (e.state !== null) {
      showSection(e.state.pageNum);
    }
  };

  $('button.back').on('click', e => {
    e.preventDefault();
    showSection(currentPage - 1);
  });


  $form.on('submit', () => {
    $(this).off();
    validate()
      .then(html =>
        checkFormErrors(html)
          .then(() => $form.off().submit())
          .catch(replaceErrors)
      ).catch(() => {}); // TODO: log ting?
    return false;
  });
});
