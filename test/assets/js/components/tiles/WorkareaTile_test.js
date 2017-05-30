import WorkareaTile from 'components/tiles/WorkareaTile';
import { shallow } from 'enzyme';

describe('WorkareaTile', () => {
  it('should pluralise "seat" according to total seats available', () => {
    const props = {
      "content": {
        "href": "http://warwick.ac.uk/",
        "items": [
          {
            id: 'es7',
            availableSeats: 1,
            totalSeats: 2,
            location: 'ITS: es7'
          },
        ]
      },
      size: 'small',
    };
    const wrapper = shallow(<WorkareaTile {...props} />);
    expect(wrapper.html()).to.contain("seats available");
  });
});
