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

/**
 * Returns single readable string from string array.
 * E.g. (['Ed', 'Edd', 'Eddy']) => 'Ed, Edd and Eddy'
 *
 * @param {string[]} list
 * @returns {string}
 */
export function mkString(list) {
  if (!list || list.length === 0) return '';
  else if (list.length === 1) return list[0];
  else return `${list.slice(0, list.length - 1).join(', ')} and ${list[list.length - 1]}`;
}

export function isiPhoneX() {
  const theWindow = window;
  const height = theWindow.screen.height;
  const width = theWindow.screen.width;
  return (height === 812 && width === 375);
}
