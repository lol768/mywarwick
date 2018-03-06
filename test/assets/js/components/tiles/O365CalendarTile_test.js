import O365CalendarTile from 'components/tiles/O365CalendarTile';
import {shallow} from 'enzyme';
import React from 'react';

describe('O365CalendarTile', () => {

  it("should link to webcalendar when 0 event items", () => {
    const props = {
      content: {
        defaultText: 'You\'ve got no events',
        items: [],
      },
      size: 'small',
    };

    const o365CalendarTile = shallow(<O365CalendarTile {...props} />);

    expect(o365CalendarTile.text()).to.contain(props.content.defaultText);
    expect(o365CalendarTile.text()).to.contain('Open calendar')
  })
});