import {mkString, pluralise} from '../../../app/assets/js/helpers';

describe ('Javascript helpers', () => {

  it ('#mkString - should join string array to single readable string', () => {
    const arr = ['Edd', 'Ed', 'Eddy', 'Nic'];
    const expected = 'Edd, Ed, Eddy, and Nic';
    const actual = mkString(arr);
    assert.equal(expected, actual);
  });

  it ('#mkString - should not error on !list', () => {
    const arrUndef = undefined;
    const arrNull = null;
    const arrEmpty = [];

    const actualUndef = mkString(arrUndef);
    const actualNull = mkString(arrNull);
    const actualEmpty = mkString(arrEmpty);

    const expected = '';
    assert.equal(expected, actualUndef);
    assert.equal(expected, actualNull);
    assert.equal(expected, actualEmpty);
  });

  it ('#mkString - should handle string array with length 1', () => {
    const arr = ['Nic'];
    const expected = 'Nic';
    const actual = mkString(arr);
    assert.equal(expected, actual);
  });

  it ('#mkString - should handle string array with length 2', () => {
    const arr = ['Nic', 'Eddy'];
    const expected = 'Nic and Eddy';
    const actual = mkString(arr);
    assert.equal(expected, actual);
  });

  it ('#pluralise - should append \'s\' to string input', () => {
    const input = 'string';
    const expected = 'strings';
    const actual = pluralise(input, 2);
    assert.equal(expected, actual);
  });

  it ('#pluralise - should use optional plural param', () => {
    const input = 'bell';
    const plural = 'jingle bells';
    const actual = pluralise(input, 2, plural);
    assert.equal(plural, actual);
  });

  it ('#pluralise - should handle unit count of 1', () => {
    const input = 'bell';
    const actual = pluralise(input, 1);
    assert.equal(input, actual);
  });

});