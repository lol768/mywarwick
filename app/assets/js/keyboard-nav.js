/* eslint-env browser */

const ENTER_KEYCODE = 13;
const SPACE_KEYCODE = 32;

/**
 * Helper for handling both keyboard and click events for
 * managed components. Handles blurring to remove outlines
 * after click, stopping space scrolling the page, only
 * triggering on space/enter etc.
 *
 * @param callback The "onSelected" callback to hit.
 * @param event The original event.
 */
export default function wrapKeyboardSelect(callback, event) {
  if (event.type === 'click') {
    event.target.blur(); // for outline removal
    callback(event);
  } else if (event.type === 'keyup' &&
    (event.keyCode === ENTER_KEYCODE || event.keyCode === SPACE_KEYCODE)) {
    event.preventDefault();
    callback(event);
  }
  // other keyboard event, do nothing
}
