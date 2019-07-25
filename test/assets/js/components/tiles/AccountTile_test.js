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
        homeDepartment: {
          code: 'IB',
          name: 'Warwick Business School',
        },
        userType: 'student',
        userSource: 'WBSLdap',
      },
      user: {},
    };

  const shallow = enzyme.shallow(<AccountTile {...props} />);

  expect(shallow.html()).to.contain('sign in with your ITS credentials instead.');
  });

  it('should not render undefined for users with no universityId', () => {
    const member = {
      fullName: 'Ext User',
      email: 'ext-user@warwick.ac.uk',
      userId: 'cu_extuser',
      homeDepartment: {},
      userType: 'Staff',
      userSource: 'WarwickExtUsers'
    };

    const userId = AccountTile.makeUserid(member);

    expect(userId).to.not.contain('undefined');
  });
});
