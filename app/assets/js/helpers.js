/* eslint-env browser */
/**
 * Appends 's' to unit, or replaces unit with plural, if count > 1
 *
 * @param {string} unit
 * @param {number} count
 * @param {string} [plural]
 * @returns {string}
 */
export function pluralise(unit, count, plural) {
  return count === 1 ? unit : (plural || `${unit}s`);
}

export function mkString(list) {
  return list.join(', ').replace(/,\s(\w+)$/, ' and $1');
}

export function isiPhoneX() {
  const theWindow = window;
  const height = theWindow.screen.height;
  const width = theWindow.screen.width;
  return (height === 812 && width === 375);
}
