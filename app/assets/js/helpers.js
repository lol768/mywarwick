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

export function isiPhoneX() {
  let height = window.screen.height;
  let width = window.screen.width;
  return (height === 812 && width === 375)
}