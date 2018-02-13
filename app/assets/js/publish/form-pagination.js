/* eslint-env browser */

import $ from 'jquery';
import log from 'loglevel';
import 'jquery-form/jquery.form';
import promiseSubmit from './utils';
import _ from 'lodash-es';

const SPLIT_FORM = 'form.split-form';
const SLIDE_DURATION = 350;

$(SPLIT_FORM).each((i, form) => {
  const $form = $(form);

  /**
   * Animates slide transition on $prev and $next elements
   * @param dir left (-1) or right (1)
   * @param $prev
   * @param $next
   */
  function transition($prev, $next, dir = -1) {
    $prev.removeClass('active')
      .animate({ marginLeft: `${100 * dir}%`, opacity: 0 }, {
        duration: SLIDE_DURATION,
        start: () => $form.addClass('split-form--transitioning'),
        complete: () => {
          $form.removeClass('split-form--transitioning');
          $prev.hide();
          $next.find('input[data-autofocus]').focus();
        },
      });

    $next.css({ marginLeft: `${-100 * dir}%`, display: 'inline-block' }).addClass('active')
      .animate({ marginLeft: '0', opacity: 1 }, { duration: SLIDE_DURATION });
  }

  function getHashNum() {
    return parseInt(location.hash.substring(1), 10);
  }

  let currentPage = getHashNum() || 0;

  function showSection(num) {
    history.replaceState({ pageNum: num }, location.href);
    const prevPage = currentPage;
    currentPage = num;
    const $entering = $form.find(`section:eq(${num})`);
    const $leaving = $form.find('section.active');

    if (prevPage === num) { // on initial load
      $entering.addClass('active').css('display', 'inline-block');
    } else if (prevPage < num) {
      transition($leaving, $entering);
    } else {
      transition($leaving, $entering, 1); // transition right
    }
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

    return promiseSubmit(form, { url: `${formAction}/validate` });
  }

  function hasErrors() {
    return $form.find(`section:eq(${currentPage}) .has-error`).length > 0;
  }

  function updateFormGroupErrors(html) {
    function prependErrorsToElem($elem, errorsAsJson, errorType) {
      $elem.prepend(
        errorsAsJson.map((err, index) =>
          $('<div>').addClass('help-block').attr('id', `${errorType}_error_${index}`)
            .html(`<i class="fa fa-exclamation-triangle"></i> ${err}`),
        ),
      );
    }

    // Remove all errors from the current page of the form
    const $currentSection = $form.find(`section:eq(${currentPage})`);
    $currentSection.find('*[id*=_error_]').remove();
    $currentSection.find('.has-error').removeClass('has-error');

    const errorsAsJson = $(html).find('.audience-picker').data('errors');
    const $audiencePicker = $('.audience-picker');
    const $categoryPicker = $('.category-picker');

    if (!_.isEmpty(errorsAsJson.audience)) {
      $audiencePicker.addClass('has-error');
      prependErrorsToElem($audiencePicker, errorsAsJson.audience, 'audience');
    }
    if (!_.isEmpty(errorsAsJson.categories)) {
      $categoryPicker.addClass('has-error');
      prependErrorsToElem($categoryPicker, errorsAsJson.categories, 'category');
    }

    const usercodeErrors = errorsAsJson['audience.usercodes'];
    if (usercodeErrors !== undefined) {
      const $listOfUsercodes = $audiencePicker.find('.listOfUsercodes');
      $listOfUsercodes.addClass('has-error');
      prependErrorsToElem($listOfUsercodes, usercodeErrors, 'audience_usercodes');
    }

    const $groupsWithErrors = $(html).find(`section:eq(${currentPage}) .has-error`);

    $.map($groupsWithErrors, (group) => {
      const $errors = $(group).find('*[id*=_error_]');
      const $groupInPage = $(`#${group.id}`);

      // Put the form group into the error state and add any errors
      $groupInPage.addClass('has-error');
      const $div = $groupInPage.find('> div').eq(0);

      // Some fields display their errors before the control
      if ($errors.index() === 0) {
        $errors.prependTo($div);
      } else {
        $errors.appendTo($div);
      }
    });
  }

  $('button.next').on('click', function onClick(e) {
    e.preventDefault();

    const $button = $(this).prop('disabled', true);

    validate()
      .then((html) => {
        updateFormGroupErrors(html);

        if (!hasErrors()) {
          pushSection(currentPage + 1);
        }
      })
      .catch(ex => log.error(ex))
      .then(() => $button.prop('disabled', false));
  });

  window.onpopstate = (e) => {
    if (e.state !== null) {
      showSection(e.state.pageNum);
    }
  };

  $('button.back').on('click', () => {
    pushSection(currentPage - 1);
  });

  const $confirmationModal = $('#confirmCreateModal');
  const $createConfirmBtn = $confirmationModal.find('.confirm');
  const $createBtn = $form.find(':submit').last();

  const confirm = () => {
    $createBtn.button('loading');
    $form.off().submit();
  };

  $createConfirmBtn.one('click', (event) => {
    event.preventDefault();
    $(event.target).attr('disabled', true);
    confirm();
  });

  $form.on('submit', () => {
    validate()
      .then((html) => {
        updateFormGroupErrors(html);

        if (!hasErrors()) {
          const baseAudience = $form.data('base-audience');
          if (baseAudience > 100) {
            const msg = `Are you sure you want to send this alert to ${baseAudience} people?`;
            $confirmationModal.find('.modal-body').text(msg);
            $confirmationModal.modal('show');
          } else { confirm(); }
        }
      })
      .catch(e => log.error(e));
    return false;
  });
});
