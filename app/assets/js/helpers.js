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
 * E.g. (['Ed', 'Edd', 'Eddy']) => 'Ed, Edd, and Eddy'
 *
 * @param {string[]} list
 * @returns {string}
 */
export function mkString(list) {
  if (!list || list.length === 0) return '';
  else if (list.length === 1) return list[0];
  return `${list.slice(0, list.length - 1).join(', ')}${list.length > 2 ? ',' : ''} and ${list[list.length - 1]}`;
}

export function lowercaseFirst(string) {
  if (string && string.length > 0) {
    return string[0].toLowerCase() + string.slice(1);
  }
  return string;
}

export function isiPhoneX() {
  const theWindow = window;
  const height = theWindow.screen.height;
  const width = theWindow.screen.width;
  return (height === 812 && width === 375);
}
