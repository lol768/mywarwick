import MailTile from 'components/tiles/MailTile';
import {shallow} from 'enzyme';
import React from 'react';

describe('MailTile', () => {

  it("should link to inbox when 0 mail items", () => {
    const props = {
      content: {
        items: [],
      },
      size: 'small',
    };

    const mailTile = shallow(<MailTile {...props} />);

    expect(mailTile.text()).to.contain('Open inbox')
  })
});