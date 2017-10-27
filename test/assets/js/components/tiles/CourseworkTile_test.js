import CourseworkTile from 'components/tiles/CourseworkTile';
import { shallow } from 'enzyme';
import { ListTileItem } from '../../../../../app/assets/js/components/tiles/ListTile';

describe('CourseworkTile', () => {

  const props = {
    content: {
      items: [
        {
          date: "2016-05-12T00:00:00+01:00",
          href: "n/a",
          id: "ed",
          text: "Parachuting (SP747)",
          title: "Parachuting",
        },
        {
          date: "2016-05-16T00:00:00+01:00",
          href: "n/a",
          id: "edd",
          text: "Tactical Espionage Action (SN4k3)",
          title: "Tactical Espionage Action",
        },
        {
          date: "2016-05-20T00:00:00+01:00",
          href: "n/a",
          id: "eddy",
          text: "Health and Safety (IN102)",
          title: "Health and Safety",
        },
      ]
    }
  };

  it('renders small tile with assignment count for next month', () => {
    const html = renderAtMoment(<CourseworkTile {...props} size="small" />, new Date(2016, 3, 20));
    findChild(html, [0, 0, 0]).should.equal(2); // because third item > 1 month in future
    findChild(html, [1, 1]).type.displayName.should.equal('Hyperlink');
    findChild(html, [1, 1]).props.children.should.equal('Parachuting (SP747)');
    findChild(html, [1, 3]).should.equal('Thu 12 May, 00:00');
  });

  it('renders large tile as List Tile list', () => {
    const render = shallow(<CourseworkTile {...props} size="large" />);
    expect(render.find(ListTileItem)).to.have.length(3);
    expect(render.find(ListTileItem).get(1).props.text).to.eql('Tactical Espionage Action (SN4k3)');
    expect(render.find(ListTileItem).get(1).props.title).to.eql('Tactical Espionage Action');
  });
});
