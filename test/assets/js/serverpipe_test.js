import { addQsToUrl } from 'serverpipe';


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

});