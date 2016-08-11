import $ from 'jquery';
import '../../../../node_modules/jquery-form/jquery.form.js';

(() => {
  const SPLIT_FORM = 'form.split-form';

  function getHashNum() {
    return parseInt(location.hash.substring(1), 10);
  }

  let currentPage = getHashNum() || 0;
  history.pushState({ pageNum: currentPage }, location.href);

  function showSection(num) {
    history.replaceState({ pageNum: num }, location.href);
    currentPage = num;
    const section = `section:eq(${num})`;
    $('.active').removeClass('active').hide();
    $(section).show().addClass('active').fadeIn();
  }

  $('section:not(:last)').append('<button class="btn btn-primary next pull-right" >Next</button>');

  showSection(currentPage);

  function validate() {
    return new Promise((resolve, reject) =>
      $(SPLIT_FORM).ajaxSubmit({
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
    history.pushState({ pageNum: currentPage }, location.href);
    validate()
      .then(html => checkFormErrors(html)
        .then(() => {
          $(`section:eq(${currentPage}) > *`).removeClass('has-error');
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

  $(SPLIT_FORM).on('submit', () => {
    $(this).off();
    validate()
      .then(html =>
        checkFormErrors(html)
          .then(() => $(SPLIT_FORM).off().submit())
          .catch(replaceErrors)
      ).catch(() => {}); // TODO: log ting?
    return false;
  });
})();
