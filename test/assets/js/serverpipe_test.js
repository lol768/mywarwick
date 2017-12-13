import { addQsToUrl, appendTimeStampToQs } from 'serverpipe';

describe('serverpipe', () => {

  it('append querystring to url', () => {

    const originalUrl = '/api/test';
    const qs = {
      cake: 'salted',
      ts: 123,
    };

    const expected = `/api/test?cake=salted&ts=123`;
    assert.equal(addQsToUrl(originalUrl, qs), expected);
  });

  it('append timestamp to a given querystring', () => {

    const qs = {
      name: 'who',
    };

    const timeStamp = 123;
    const result = appendTimeStampToQs(qs, timeStamp);
    const expected = {
      name: 'who',
      ts: 123,
    };

    assert.deepEqual(result, expected);

  })

});