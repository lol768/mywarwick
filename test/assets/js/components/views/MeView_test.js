import MeView from 'components/views/MeView';
import { shallow } from 'enzyme';
import TileView from 'components/views/TileView';

describe('MeView', () => {
  const state = {
    tiles: {
      data: {
        tiles: [
          {
            id: 'map',
            colour: 0,
            icon: 'map',
            preferences: null,
            title: 'Campus Map',
            type: 'map',
            removed: true,
            needsFetch: false
          },
          {
            id: 'uni-events',
            colour: 1,
            icon: 'calendar-o',
            preferences: null,
            title: 'Events',
            type: 'agenda',
            removed: true,
            needsFetch: true
          },
          {
            id: 'bus',
            colour: 0,
            icon: 'bus',
            preferences: null,
            title: 'Buses',
            type: 'text',
            removed: false,
            needsFetch: true
          },
          {
            id: 'coursework',
            colour: 1,
            icon: 'file-text-o',
            preferences: null,
            title: 'Coursework',
            type: 'coursework',
            removed: false,
            needsFetch: true
          },
          // these 3 not rendered as tile view
          {
            id: 'news',
            colour: 0,
            icon: 'newspaper-o',
            preferences: null,
            title: 'News',
            type: 'news',
            removed: false,
            needsFetch: false
          },
          {
            id: 'activity',
            colour: 1,
            icon: 'dashboard',
            preferences: null,
            title: 'Activity',
            type: 'activity',
            removed: false,
            needsFetch: false
          },
          {
            id: 'notifications',
            colour: 2,
            icon: 'inbox',
            preferences: null,
            title: 'Notifications',
            type: 'notifications',
            removed: false,
            needsFetch: false
          },

          // these should not be rendered
          {
            id: 'non-existing',
            colour: 2,
            icon: 'inbox',
            preferences: null,
            title: 'non-existing',
            type: 'non-existing',
            removed: false,
            needsFetch: false,
          },
          {
            id: 'non-existing-2',
            colour: 2,
            icon: 'inbox',
            preferences: null,
            title: 'non-existing-2',
            type: 'non-existing-2',
            removed: false,
            needsFetch: false,
          }

        ],
      }
    },
  };

  it('should not render unknown tiles or removed tiles', () => {
    const props = {
      layoutWidth: 2,
      tiles: state.tiles.data.tiles,
      layout: [],
      deviceWidth: 428,
      dispatch: () => {},
    };
    const wrapper = shallow(<MeView.WrappedComponent {...props} />);
    const tileViews = wrapper.find(TileView);
    expect(tileViews).to.have.length(2);
    const renderedTileIds = tileViews.map(node => node.key());
    const expectedTileIds = [
      'bus',
      'coursework',
    ];
    expect(renderedTileIds).to.eql(expectedTileIds);
  });

});
