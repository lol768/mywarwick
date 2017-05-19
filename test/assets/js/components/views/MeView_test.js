import MeView from 'components/views/MeView';
import { shallow } from 'enzyme';
import TileView from 'components/views/TileView';

describe('MeView', () => {
  const state = {
    tiles: {
      fetching: false,
      fetched: true,
      failed: false,
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
            preferences: {
              stops: [
                '43000065301',
                '43001060901',
                '43000065202',
                '43000065302',
                '43000119602',
                '43001063002',
                '4200F157740',
                '4200F106602',
                '43000065303',
                '43000065304',
                '43000065305'
              ],
              routes: [
                'U2',
                'U17',
                '360A',
                '360C',
                '11U',
                '12X',
                'X16',
                'W1C',
                '87'
              ]
            },
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
          {
            id: 'eating',
            colour: 2,
            icon: 'cutlery',
            preferences: null,
            title: 'Eating',
            type: 'text',
            removed: false,
            needsFetch: true
          },
          {
            id: 'print',
            colour: 3,
            icon: 'print',
            preferences: null,
            title: 'Print Balance',
            type: 'text',
            removed: false,
            needsFetch: true
          },
          {
            id: 'weather',
            colour: 2,
            icon: 'sun-o',
            preferences: {
              location: 'warwick-campus-uk'
            },
            title: 'Weather',
            type: 'weather',
            removed: false,
            needsFetch: true
          },
          {
            id: 'timetable',
            colour: 0,
            icon: 'calendar',
            preferences: null,
            title: 'Teaching Timetable',
            type: 'agenda',
            removed: false,
            needsFetch: true
          },
          {
            id: 'traffic',
            colour: 0,
            icon: 'car',
            preferences: null,
            title: 'Traffic',
            type: 'traffic',
            removed: false,
            needsFetch: true
          },
          {
            id: 'library',
            colour: 3,
            icon: 'book',
            preferences: null,
            title: 'Library',
            type: 'library',
            removed: false,
            needsFetch: true
          },
          {
            id: 'mail',
            colour: 0,
            icon: 'envelope-o',
            preferences: null,
            title: 'O365 Mail',
            type: 'list',
            removed: false,
            needsFetch: true
          },
          {
            id: 'calendar',
            colour: 1,
            icon: 'calendar',
            preferences: null,
            title: 'O365 Calendar',
            type: 'agenda',
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
        layout: [
          {
            tile: 'activity',
            layoutWidth: 5,
            x: 2,
            y: 4,
            width: 2,
            height: 4
          },
          {
            tile: 'bus',
            layoutWidth: 2,
            x: 0,
            y: 4,
            width: 1,
            height: 1
          },
          {
            tile: 'bus',
            layoutWidth: 5,
            x: 0,
            y: 1,
            width: 2,
            height: 2
          },
          {
            tile: 'calendar',
            layoutWidth: 2,
            x: 0,
            y: 5,
            width: 1,
            height: 1
          },
          {
            tile: 'calendar',
            layoutWidth: 5,
            x: 4,
            y: 6,
            width: 1,
            height: 1
          },
          {
            tile: 'coursework',
            layoutWidth: 2,
            x: 0,
            y: 2,
            width: 2,
            height: 2
          },
          {
            tile: 'coursework',
            layoutWidth: 5,
            x: 1,
            y: 0,
            width: 1,
            height: 1
          },
          {
            tile: 'eating',
            layoutWidth: 2,
            x: 1,
            y: 6,
            width: 1,
            height: 1
          },
          {
            tile: 'eating',
            layoutWidth: 5,
            x: 4,
            y: 4,
            width: 1,
            height: 1
          },
          {
            tile: 'library',
            layoutWidth: 2,
            x: 0,
            y: 0,
            width: 2,
            height: 1
          },
          {
            tile: 'library',
            layoutWidth: 5,
            x: 2,
            y: 8,
            width: 2,
            height: 2
          },
          {
            tile: 'mail',
            layoutWidth: 2,
            x: 1,
            y: 1,
            width: 1,
            height: 1
          },
          {
            tile: 'mail',
            layoutWidth: 5,
            x: 4,
            y: 1,
            width: 1,
            height: 1
          },
          {
            tile: 'news',
            layoutWidth: 5,
            x: 0,
            y: 3,
            width: 2,
            height: 4
          },
          {
            tile: 'notifications',
            layoutWidth: 5,
            x: 2,
            y: 0,
            width: 2,
            height: 4
          },
          {
            tile: 'print',
            layoutWidth: 2,
            x: 0,
            y: 6,
            width: 1,
            height: 1
          },
          {
            tile: 'print',
            layoutWidth: 5,
            x: 4,
            y: 0,
            width: 1,
            height: 1
          },
          {
            tile: 'timetable',
            layoutWidth: 2,
            x: 0,
            y: 1,
            width: 1,
            height: 1
          },
          {
            tile: 'timetable',
            layoutWidth: 5,
            x: 0,
            y: 7,
            width: 2,
            height: 2
          },
          {
            tile: 'traffic',
            layoutWidth: 2,
            x: 1,
            y: 5,
            width: 1,
            height: 1
          },
          {
            tile: 'traffic',
            layoutWidth: 5,
            x: 4,
            y: 2,
            width: 1,
            height: 1
          },
          {
            tile: 'uni-events',
            layoutWidth: 2,
            x: 0,
            y: 7,
            width: 2,
            height: 1
          },
          {
            tile: 'uni-events',
            layoutWidth: 5,
            x: 4,
            y: 5,
            width: 1,
            height: 1
          },
          {
            tile: 'weather',
            layoutWidth: 2,
            x: 1,
            y: 4,
            width: 1,
            height: 1
          },
          {
            tile: 'weather',
            layoutWidth: 5,
            x: 4,
            y: 3,
            width: 1,
            height: 1
          }
        ],
      }
    },
    update: {
      isUpdateReady: false
    },
    ui: {
      className: 'mobile',
      isWideLayout: false,
      colourTheme: 'default',
      'native': false,
      showBetaWarning: false
    },
    device: {
      pixelWidth: 960,
      width: 428,
      notificationPermission: 'granted'
    },
  };

  const props = {
    isDesktop: state.ui.className === 'desktop',
    layoutWidth: state.ui.isWideLayout === true ? 5 : 2,
    tiles: state.tiles.data.tiles,
    layout: state.tiles.data.layout,
    deviceWidth: state.device.width,
    navRequest: state.ui.navRequest,
    dispatch: () => {
    },
  };

  it('should not render unknown tiles or removed tiles', () => {
    const wrapper = shallow(<MeView.WrappedComponent {...props} />);
    expect(wrapper.find(TileView)).to.have.length(10);

  });

});
