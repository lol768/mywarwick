import React from 'react';
import AccountTile from 'components/tiles/AccountTile';
import * as enzyme from 'enzyme';

describe('AccountTile', () => {

  it('render message for WBS users', () => {
    const props = {
      size: 'large', // WBS 'sign in with ITS account' msg to be shown on large tile only
      content: {
        fullName: 'President Business',
        email: '',
        userId: 'wbs123',
        universityId: '1234567',
        homeDepartment: {},
        userType: 'student',
        usersource: 'WBSLdap',
      },
      user: {},
    };

  const shallow = enzyme.shallow(<AccountTile {...props} />);

  expect(shallow.html()).to.contain('Please sign in with your ITS account');
  });

});