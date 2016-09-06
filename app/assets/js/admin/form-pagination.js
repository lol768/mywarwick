import $ from 'jquery';
import { promiseSubmit } from './utils';
import 'jquery-form/jquery.form.js';
import log from 'loglevel';

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
        start: () => $form.css('overflow', 'hidden'),
        complete: () => {
          $form.css('overflow', 'visible');
          $prev.hide();
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

  function replaceFormGroups(html) {
    $(html).find(`section:eq(${currentPage}) .form-group[id]`).each((j, group) => {
      $form.find(`#${group.id}`).replaceWith(group);
    });

    $(document).trigger('start:replace');
  }

  $('button.next').on('click', function onClick(e) {
    e.preventDefault();

    const $button = $(this).prop('disabled', true);

    validate()
      .then(html => {
        replaceFormGroups(html);

        if (!hasErrors()) {
          pushSection(currentPage + 1);
        }
      })
      .catch(ex => log.error(ex))
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
      .then(html => {
        replaceFormGroups(html);

        if (!hasErrors()) {
          $form.off().submit();
        }
      })
      .catch(e => log.error(e));
    return false;
  });
});
