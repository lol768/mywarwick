import * as userinfo from 'userinfo-base';

describe('user', () => {

  it('converts login URL', () => {
    const refresh = 'https://login.example.com/?target=badloc&providerId=Kevin';
    const currentLoc = 'https://my.warwick.ac.uk/alerts?x=2';
    const expected = 'https://login.example.com/?target=https%3A%2F%2Fmy.warwick.ac.uk%2Falerts%3Fx%3D2&providerId=Kevin&myWarwickRefresh=true';
    userinfo.rewriteRefreshUrl(refresh,currentLoc).should.equal(expected);
  });

});