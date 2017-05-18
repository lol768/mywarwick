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
        options: {
          map: {},
          'uni-events': {
            calendars: {
              type: 'array',
              description: 'Show events from these calendars',
              options: [
                {
                  name: 'Arts Centre',
                  value: 'arts-centre'
                },
                {
                  name: 'Warwick Sport',
                  value: 'sports-centre'
                },
                {
                  name: 'Insite',
                  value: 'insite'
                },
                {
                  name: 'Warwick Student Cinema',
                  value: 'warwick-student-cinema'
                }
              ],
              'default': [
                'sports-centre',
                'insite',
                'arts-centre',
                'warwick-student-cinema'
              ]
            }
          },
          bus: {
            stops: {
              type: 'array',
              description: 'Show departures from these stops',
              options: [
                {
                  name: 'Interchange (Stop UW1)',
                  value: '43000065301'
                },
                {
                  name: 'Automotive Centre (Stop UR3)',
                  value: '43000065202'
                },
                {
                  name: 'Interchange (Stop UW2)',
                  value: '43000065302'
                },
                {
                  name: 'Interchange (Stop UW3)',
                  value: '43000065303'
                },
                {
                  name: 'Interchange (Stop UW4)',
                  value: '43000065304'
                },
                {
                  name: 'Interchange (Stop UW5)',
                  value: '43000065305'
                },
                {
                  name: 'Gibbet Hill Campus (Stop GH6)',
                  value: '43000119601'
                },
                {
                  name: 'Gibbet Hill Campus (Stop GH5)',
                  value: '43000119602'
                },
                {
                  name: 'University Security Office (Stop UR1)',
                  value: '43001060901'
                },
                {
                  name: 'University Security Office',
                  value: '43001060902'
                },
                {
                  name: 'Scarman Road (Stop GH3)',
                  value: '43001317101'
                },
                {
                  name: 'Scarman Road (Stop GH4)',
                  value: '43001317102'
                }
              ],
              'default': [
                '43000065301',
                '43000065302',
                '43000065303',
                '43000065304',
                '43000065305'
              ]
            },
            routes: {
              type: 'array',
              description: 'Show only departures of these routes',
              options: [
                {
                  value: '43'
                },
                {
                  value: '11U'
                },
                {
                  value: '11'
                },
                {
                  value: '60'
                },
                {
                  value: 'BHX'
                },
                {
                  value: '12X'
                },
                {
                  value: 'OXF'
                },
                {
                  value: 'U1'
                },
                {
                  value: '43W'
                },
                {
                  value: '87'
                }
              ],
              'default': []
            }
          },
          coursework: {},
          eating: {},
          print: {},
          weather: {
            location: {
              type: 'string',
              description: 'show weather report for this location',
              options: [
                {
                  name: 'Roseville, CA',
                  value: 'roseville-usa'
                },
                {
                  name: 'Warwick University campus',
                  value: 'warwick-campus-uk'
                }
              ],
              'default': 'warwick-campus-uk'
            }
          },
          timetable: {},
          traffic: {},
          library: {},
          mail: {},
          calendar: {},
          news: {},
          activity: {},
          notifications: {}
        }
      }
    },
    tileContent: {
      'uni-events': {
        content: {
          defaultText: 'There are no upcoming events to show',
          items: [
            {
              id: '20170508-8a17841a5b90df0c015ba9a5195a11e8@warwick.ac.uk',
              source: 'sports-centre',
              start: '2017-05-07T23:00:00.000Z',
              end: '2017-05-21T22:55:00.000Z',
              isAllDay: false,
              title: 'Running & Swimming Grid Challenge',
              location: {
                name: 'Various'
              },
              href: 'http://www2.warwick.ac.uk/services/sport/events/rgc-sgc/'
            },
            {
              id: '20170518-094d434557c35393015829da96c57eaa@warwick.ac.uk',
              source: 'insite',
              start: '2017-05-17T23:00:00.000Z',
              isAllDay: true,
              title: 'RCGP Midland Faculty Annual Education, Research and Innovation Symposium 2017',
              location: {
                name: 'Westwood Teaching Centre'
              },
              href: 'http://www2.warwick.ac.uk/fac/med/about/centres/wpc/rcgp2017'
            },
            {
              id: '20170518-8a17841a5b90df0c015ba55cf1385a13@warwick.ac.uk',
              source: 'insite',
              start: '2017-05-18T11:00:00.000Z',
              end: '2017-05-18T13:00:00.000Z',
              isAllDay: false,
              title: 'Dementia awareness talk',
              location: {
                name: 'Ramphal R0.12'
              },
              href: 'http://www2.warwick.ac.uk/services/equalops/diversityatwarwickevent'
            },
            {
              id: 'uk.co.warwickartscentre.www.13957',
              source: 'arts-centre',
              start: '2017-05-18T11:00:00.000Z',
              end: '2017-05-18T20:00:00.000Z',
              isAllDay: false,
              title: 'Room',
              location: {
                name: 'Mead Gallery'
              },
              href: 'https://www.warwickartscentre.co.uk/whats-on/2017/room-2/'
            },
            {
              id: 'uk.co.warwickartscentre.www.13936',
              source: 'arts-centre',
              start: '2017-05-18T12:10:00.000Z',
              end: '2017-05-18T12:10:00.000Z',
              isAllDay: false,
              title: 'Lunchtime Concerts Summer 2017 ',
              location: {
                name: 'Ensemble Room'
              },
              href: 'https://www.warwickartscentre.co.uk/whats-on/2017/lunchtime-concerts-summer-2017-/'
            },
            {
              id: 'uk.co.warwickartscentre.www.14248',
              source: 'arts-centre',
              start: '2017-05-18T15:00:00.000Z',
              end: '2017-05-18T15:00:00.000Z',
              isAllDay: false,
              title: 'Rules Don\'t Apply ',
              location: {
                name: 'Cinema'
              },
              href: 'https://www.warwickartscentre.co.uk/whats-on/2017/rules-dont-apply-/'
            },
            {
              id: '20170518-8a17841b5c0c782d015c1708bad97da9@warwick.ac.uk',
              source: 'insite',
              start: '2017-05-18T17:00:00.000Z',
              end: '2017-05-18T19:00:00.000Z',
              isAllDay: false,
              title: 'Europe in Question Round Table: Crossing Borders in a Turbulent European Union',
              location: {
                name: 'The Oculus, OC0.04'
              },
              href: 'https://www.eventbrite.co.uk/e/europe-in-question-round-table-crossing-borders-in-a-turbulent-eu-tickets-33208758290'
            },
            {
              id: '20170518-8a17841a5b3d6cd7015b5dd19f1457ca@warwick.ac.uk',
              source: 'insite',
              start: '2017-05-18T17:00:00.000Z',
              end: '2017-05-18T19:00:00.000Z',
              isAllDay: false,
              title: 'Celebrating Fascination of Plants Day 2017',
              location: {
                name: 'Wellesbourne campus'
              },
              href: 'http://www2.warwick.ac.uk/fac/sci/lifesci/wcc/events/fascinationplantsday'
            },
            {
              id: 'uk.co.warwickartscentre.www.14000',
              source: 'arts-centre',
              start: '2017-05-18T18:00:00.000Z',
              end: '2017-05-18T18:00:00.000Z',
              isAllDay: false,
              title: 'NT Live: Who\'s Afraid of Virginia Woolf? (live screening)',
              location: {
                name: 'Cinema'
              },
              href: 'https://www.warwickartscentre.co.uk/whats-on/2017/nt-live-whos-afraid-of-virginia-woolf-live-screening/'
            },
            {
              id: 'uk.co.warwickartscentre.www.13741',
              source: 'arts-centre',
              start: '2017-05-18T18:30:00.000Z',
              end: '2017-05-18T18:30:00.000Z',
              isAllDay: false,
              title: 'Ventoux',
              location: {
                name: 'Studio'
              },
              href: 'https://www.warwickartscentre.co.uk/whats-on/2017/ventoux/'
            },
            {
              id: 'WSCFILMSCREENING@SCR_ID6388',
              source: 'warwick-student-cinema',
              start: '2017-05-18T18:30:00.000Z',
              isAllDay: false,
              title: 'Elle',
              location: {
                name: 'Lecture Theatre 3, (Science Concourse)'
              },
              href: 'https://warwick.film/filminfo?id=3471'
            },
            {
              id: 'uk.co.warwickartscentre.www.13388',
              source: 'arts-centre',
              start: '2017-05-18T18:45:00.000Z',
              end: '2017-05-18T18:45:00.000Z',
              isAllDay: false,
              title: 'Miss Meena and the Masala Queens ',
              location: {
                name: 'Theatre'
              },
              href: 'https://www.warwickartscentre.co.uk/whats-on/2017/miss-meena-and-the-masala-queens-/'
            },
            {
              id: 'uk.co.warwickartscentre.www.13958',
              source: 'arts-centre',
              start: '2017-05-19T11:00:00.000Z',
              end: '2017-05-19T20:00:00.000Z',
              isAllDay: false,
              title: 'Room',
              location: {
                name: 'Mead Gallery'
              },
              href: 'https://www.warwickartscentre.co.uk/whats-on/2017/room-2/'
            },
            {
              id: '20170519-8a17841b5c0c782d015c0ffc905e500f@warwick.ac.uk',
              source: 'insite',
              start: '2017-05-19T13:00:00.000Z',
              end: '2017-05-19T14:00:00.000Z',
              isAllDay: false,
              title: 'Life Sciences seminar by Dr Mark Wall',
              location: {
                name: 'GLT2, Gibbet Hill campus'
              }
            },
            {
              id: 'uk.co.warwickartscentre.www.14278',
              source: 'arts-centre',
              start: '2017-05-19T17:30:00.000Z',
              end: '2017-05-19T17:30:00.000Z',
              isAllDay: false,
              title: 'Lady Macbeth ',
              location: {
                name: 'Cinema'
              },
              href: 'https://www.warwickartscentre.co.uk/whats-on/2017/lady-macbeth-/'
            },
            {
              id: 'WSCFILMSCREENING@SCR_ID6405',
              source: 'warwick-student-cinema',
              start: '2017-05-19T17:30:00.000Z',
              isAllDay: false,
              title: 'The Lego Batman Movie',
              location: {
                name: 'Lecture Theatre 3, (Science Concourse)'
              },
              href: 'https://warwick.film/filminfo?id=3483'
            },
            {
              id: 'uk.co.warwickartscentre.www.13742',
              source: 'arts-centre',
              start: '2017-05-19T18:30:00.000Z',
              end: '2017-05-19T18:30:00.000Z',
              isAllDay: false,
              title: 'Ventoux',
              location: {
                name: 'Studio'
              },
              href: 'https://www.warwickartscentre.co.uk/whats-on/2017/ventoux/'
            },
            {
              id: 'uk.co.warwickartscentre.www.13389',
              source: 'arts-centre',
              start: '2017-05-19T18:45:00.000Z',
              end: '2017-05-19T18:45:00.000Z',
              isAllDay: false,
              title: 'Miss Meena and the Masala Queens ',
              location: {
                name: 'Theatre'
              },
              href: 'https://www.warwickartscentre.co.uk/whats-on/2017/miss-meena-and-the-masala-queens-/'
            },
            {
              id: 'uk.co.warwickartscentre.www.14282',
              source: 'arts-centre',
              start: '2017-05-19T19:30:00.000Z',
              end: '2017-05-19T19:30:00.000Z',
              isAllDay: false,
              title: 'Guardians of the Galaxy Vol.2 ',
              location: {
                name: 'Cinema'
              },
              href: 'https://www.warwickartscentre.co.uk/whats-on/2017/guardians-of-the-galaxy-vol.2-/'
            },
            {
              id: 'uk.co.warwickartscentre.www.14036',
              source: 'arts-centre',
              start: '2017-05-19T20:30:00.000Z',
              end: '2017-05-19T20:30:00.000Z',
              isAllDay: false,
              title: 'Ventoux',
              location: {
                name: 'Studio'
              },
              href: 'https://www.warwickartscentre.co.uk/whats-on/2017/ventoux/'
            },
            {
              id: 'WSCFILMSCREENING@SCR_ID6406',
              source: 'warwick-student-cinema',
              start: '2017-05-19T20:30:00.000Z',
              isAllDay: false,
              title: 'The Lego Batman Movie',
              location: {
                name: 'Lecture Theatre 3, (Science Concourse)'
              },
              href: 'https://warwick.film/filminfo?id=3483'
            },
            {
              id: '20170520-094d4345578c584601578f9c535c57ab@warwick.ac.uk',
              source: 'insite',
              start: '2017-05-19T23:00:00.000Z',
              isAllDay: true,
              title: 'Bodies in Flux: Rewriting the Body in Medieval Literature, Art and Culture 1000-1450',
              location: {
                name: 'Wolfson Research Exchange'
              },
              href: 'http://www2.warwick.ac.uk/fac/arts/hrc/confs/bif/'
            },
            {
              id: 'uk.co.warwickartscentre.www.13903',
              source: 'arts-centre',
              start: '2017-05-20T09:00:00.000Z',
              end: '2017-05-20T09:00:00.000Z',
              isAllDay: false,
              title: 'Warwick Masterclass: An Introduction to Speciality Coffee',
              location: {
                name: 'Helen Martin Studio'
              },
              href: 'https://www.warwickartscentre.co.uk/whats-on/2017/warwick-masterclass-an-introduction-to-speciality-coffee/'
            },
            {
              id: 'uk.co.warwickartscentre.www.13959',
              source: 'arts-centre',
              start: '2017-05-20T11:00:00.000Z',
              end: '2017-05-20T20:00:00.000Z',
              isAllDay: false,
              title: 'Room',
              location: {
                name: 'Mead Gallery'
              },
              href: 'https://www.warwickartscentre.co.uk/whats-on/2017/room-2/'
            },
            {
              id: 'uk.co.warwickartscentre.www.13911',
              source: 'arts-centre',
              start: '2017-05-20T11:00:00.000Z',
              end: '2017-05-20T11:00:00.000Z',
              isAllDay: false,
              title: 'Exhibition Tour: Room',
              location: {
                name: 'Mead Gallery'
              },
              href: 'https://www.warwickartscentre.co.uk/whats-on/2017/exhibition-tour-room/'
            },
            {
              id: 'uk.co.warwickartscentre.www.13993',
              source: 'arts-centre',
              start: '2017-05-20T12:30:00.000Z',
              end: '2017-05-20T12:30:00.000Z',
              isAllDay: false,
              title: 'Family Film: Fantastic Beasts and Where to Find Them',
              location: {
                name: 'Cinema'
              },
              href: 'https://www.warwickartscentre.co.uk/whats-on/2017/family-film-fantastic-beasts-and-where-to-find-them/'
            },
            {
              id: 'uk.co.warwickartscentre.www.13904',
              source: 'arts-centre',
              start: '2017-05-20T13:00:00.000Z',
              end: '2017-05-20T13:00:00.000Z',
              isAllDay: false,
              title: 'Warwick Masterclass: An Introduction to Speciality Coffee',
              location: {
                name: 'Helen Martin Studio'
              },
              href: 'https://www.warwickartscentre.co.uk/whats-on/2017/warwick-masterclass-an-introduction-to-speciality-coffee/'
            },
            {
              id: 'uk.co.warwickartscentre.www.13390',
              source: 'arts-centre',
              start: '2017-05-20T13:30:00.000Z',
              end: '2017-05-20T13:30:00.000Z',
              isAllDay: false,
              title: 'Miss Meena and the Masala Queens ',
              location: {
                name: 'Theatre'
              },
              href: 'https://www.warwickartscentre.co.uk/whats-on/2017/miss-meena-and-the-masala-queens-/'
            },
            {
              id: 'uk.co.warwickartscentre.www.14279',
              source: 'arts-centre',
              start: '2017-05-20T15:10:00.000Z',
              end: '2017-05-20T15:10:00.000Z',
              isAllDay: false,
              title: 'Lady Macbeth ',
              location: {
                name: 'Cinema'
              },
              href: 'https://www.warwickartscentre.co.uk/whats-on/2017/lady-macbeth-/'
            },
            {
              id: 'uk.co.warwickartscentre.www.14283',
              source: 'arts-centre',
              start: '2017-05-20T17:05:00.000Z',
              end: '2017-05-20T17:05:00.000Z',
              isAllDay: false,
              title: 'Guardians of the Galaxy Vol.2 ',
              location: {
                name: 'Cinema'
              },
              href: 'https://www.warwickartscentre.co.uk/whats-on/2017/guardians-of-the-galaxy-vol.2-/'
            },
            {
              id: 'WSCFILMSCREENING@SCR_ID6389',
              source: 'warwick-student-cinema',
              start: '2017-05-20T17:30:00.000Z',
              isAllDay: false,
              title: 'Moonlight',
              location: {
                name: 'Lecture Theatre 3, (Science Concourse)'
              },
              href: 'https://warwick.film/filminfo?id=3472'
            },
            {
              id: 'uk.co.warwickartscentre.www.13755',
              source: 'arts-centre',
              start: '2017-05-20T18:30:00.000Z',
              end: '2017-05-20T18:30:00.000Z',
              isAllDay: false,
              title: 'CANCELLED: Mark Nevin & Band',
              location: {
                name: 'Studio'
              },
              href: 'https://www.warwickartscentre.co.uk/whats-on/2017/cancelled-mark-nevin-and-band/'
            },
            {
              id: 'uk.co.warwickartscentre.www.13391',
              source: 'arts-centre',
              start: '2017-05-20T18:45:00.000Z',
              end: '2017-05-20T18:45:00.000Z',
              isAllDay: false,
              title: 'Miss Meena and the Masala Queens ',
              location: {
                name: 'Theatre'
              },
              href: 'https://www.warwickartscentre.co.uk/whats-on/2017/miss-meena-and-the-masala-queens-/'
            },
            {
              id: 'uk.co.warwickartscentre.www.13151',
              source: 'arts-centre',
              start: '2017-05-20T19:00:00.000Z',
              end: '2017-05-20T19:00:00.000Z',
              isAllDay: false,
              title: 'Henning Wehn: Westphalia is not an option',
              location: {
                name: 'Butterworth Hall'
              },
              href: 'https://www.warwickartscentre.co.uk/whats-on/2017/henning-wehn-westphalia-is-not-an-option/'
            },
            {
              id: 'uk.co.warwickartscentre.www.14280',
              source: 'arts-centre',
              start: '2017-05-20T19:50:00.000Z',
              end: '2017-05-20T19:50:00.000Z',
              isAllDay: false,
              title: 'Lady Macbeth ',
              location: {
                name: 'Cinema'
              },
              href: 'https://www.warwickartscentre.co.uk/whats-on/2017/lady-macbeth-/'
            },
            {
              id: 'WSCFILMSCREENING@SCR_ID6390',
              source: 'warwick-student-cinema',
              start: '2017-05-20T20:30:00.000Z',
              isAllDay: false,
              title: 'Moonlight',
              location: {
                name: 'Lecture Theatre 3, (Science Concourse)'
              },
              href: 'https://warwick.film/filminfo?id=3472'
            },
            {
              id: 'uk.co.warwickartscentre.www.14281',
              source: 'arts-centre',
              start: '2017-05-21T15:00:00.000Z',
              end: '2017-05-21T15:00:00.000Z',
              isAllDay: false,
              title: 'Lady Macbeth ',
              location: {
                name: 'Cinema'
              },
              href: 'https://www.warwickartscentre.co.uk/whats-on/2017/lady-macbeth-/'
            },
            {
              id: 'uk.co.warwickartscentre.www.14284',
              source: 'arts-centre',
              start: '2017-05-21T18:30:00.000Z',
              end: '2017-05-21T18:30:00.000Z',
              isAllDay: false,
              title: 'Guardians of the Galaxy Vol.2 ',
              location: {
                name: 'Cinema'
              },
              href: 'https://www.warwickartscentre.co.uk/whats-on/2017/guardians-of-the-galaxy-vol.2-/'
            },
            {
              id: 'uk.co.warwickartscentre.www.13372',
              source: 'arts-centre',
              start: '2017-05-21T18:45:00.000Z',
              end: '2017-05-21T18:45:00.000Z',
              isAllDay: false,
              title: 'Tez Ilyas - Made in Britain ',
              location: {
                name: 'Studio'
              },
              href: 'https://www.warwickartscentre.co.uk/whats-on/2017/tez-ilyas-made-in-britain-/'
            },
            {
              id: '20170522-8a17841a5baeba47015bb3dca0b05188@warwick.ac.uk',
              source: 'insite',
              start: '2017-05-22T10:00:00.000Z',
              end: '2017-05-22T10:45:00.000Z',
              isAllDay: false,
              title: 'Her Excellency’s Lecture - Could the Sustainable Development Government goals be a Path to Positive Peace?',
              location: {
                name: 'IDL - International Digital Lab'
              },
              href: 'http://www2.warwick.ac.uk/global/partnerships/contact/maltalecture/'
            },
            {
              id: 'uk.co.warwickartscentre.www.13960',
              source: 'arts-centre',
              start: '2017-05-22T11:00:00.000Z',
              end: '2017-05-22T20:00:00.000Z',
              isAllDay: false,
              title: 'Room',
              location: {
                name: 'Mead Gallery'
              },
              href: 'https://www.warwickartscentre.co.uk/whats-on/2017/room-2/'
            },
            {
              id: 'uk.co.warwickartscentre.www.14285',
              source: 'arts-centre',
              start: '2017-05-22T17:00:00.000Z',
              end: '2017-05-22T17:00:00.000Z',
              isAllDay: false,
              title: 'Guardians of the Galaxy Vol.2 ',
              location: {
                name: 'Cinema'
              },
              href: 'https://www.warwickartscentre.co.uk/whats-on/2017/guardians-of-the-galaxy-vol.2-/'
            },
            {
              id: 'uk.co.warwickartscentre.www.13653',
              source: 'arts-centre',
              start: '2017-05-22T18:00:00.000Z',
              end: '2017-05-22T18:00:00.000Z',
              isAllDay: false,
              title: 'String Explosion 2017 ',
              location: {
                name: 'Butterworth Hall'
              },
              href: 'https://www.warwickartscentre.co.uk/whats-on/2017/string-explosion-2017-/'
            },
            {
              id: 'uk.co.warwickartscentre.www.14253',
              source: 'arts-centre',
              start: '2017-05-22T19:50:00.000Z',
              end: '2017-05-22T19:50:00.000Z',
              isAllDay: false,
              title: 'Mad to be Normal ',
              location: {
                name: 'Cinema'
              },
              href: 'https://www.warwickartscentre.co.uk/whats-on/2017/mad-to-be-normal-/'
            },
            {
              id: 'uk.co.warwickartscentre.www.13961',
              source: 'arts-centre',
              start: '2017-05-23T11:00:00.000Z',
              end: '2017-05-23T20:00:00.000Z',
              isAllDay: false,
              title: 'Room',
              location: {
                name: 'Mead Gallery'
              },
              href: 'https://www.warwickartscentre.co.uk/whats-on/2017/room-2/'
            },
            {
              id: 'uk.co.warwickartscentre.www.14254',
              source: 'arts-centre',
              start: '2017-05-23T17:15:00.000Z',
              end: '2017-05-23T17:15:00.000Z',
              isAllDay: false,
              title: 'Mad to be Normal ',
              location: {
                name: 'Cinema'
              },
              href: 'https://www.warwickartscentre.co.uk/whats-on/2017/mad-to-be-normal-/'
            },
            {
              id: 'uk.co.warwickartscentre.www.13127',
              source: 'arts-centre',
              start: '2017-05-23T18:30:00.000Z',
              end: '2017-05-23T18:30:00.000Z',
              isAllDay: false,
              title: 'Super Sunday',
              location: {
                name: 'Theatre'
              },
              href: 'https://www.warwickartscentre.co.uk/whats-on/2017/super-sunday/'
            },
            {
              id: 'uk.co.warwickartscentre.www.13754',
              source: 'arts-centre',
              start: '2017-05-23T18:45:00.000Z',
              end: '2017-05-23T18:45:00.000Z',
              isAllDay: false,
              title: 'An Inner Voice & Cicadas',
              location: {
                name: 'Studio'
              },
              href: 'https://www.warwickartscentre.co.uk/whats-on/2017/an-inner-voice-and-cicadas/'
            },
            {
              id: 'uk.co.warwickartscentre.www.14286',
              source: 'arts-centre',
              start: '2017-05-23T19:30:00.000Z',
              end: '2017-05-23T19:30:00.000Z',
              isAllDay: false,
              title: 'Guardians of the Galaxy Vol.2 ',
              location: {
                name: 'Cinema'
              },
              href: 'https://www.warwickartscentre.co.uk/whats-on/2017/guardians-of-the-galaxy-vol.2-/'
            },
            {
              id: '20170524-8a17841b5b3d69e2015b8b513ab01976@warwick.ac.uk',
              source: 'insite',
              start: '2017-05-24T09:00:00.000Z',
              end: '2017-05-24T14:30:00.000Z',
              isAllDay: false,
              title: 'Industry Day',
              location: {
                name: 'Central Campus'
              },
              href: 'http://www2.warwick.ac.uk/research/priorities/materials/newsandevents/industryday/'
            },
            {
              id: 'uk.co.warwickartscentre.www.13962',
              source: 'arts-centre',
              start: '2017-05-24T11:00:00.000Z',
              end: '2017-05-24T20:00:00.000Z',
              isAllDay: false,
              title: 'Room',
              location: {
                name: 'Mead Gallery'
              },
              href: 'https://www.warwickartscentre.co.uk/whats-on/2017/room-2/'
            },
            {
              id: '20170524-094d43f55a573c76015a5b1e20b90c72@warwick.ac.uk',
              source: 'insite',
              start: '2017-05-24T13:00:00.000Z',
              end: '2017-05-24T14:30:00.000Z',
              isAllDay: false,
              title: 'Placements Practice Group',
              location: {
                name: 'SMR 1.13b'
              }
            },
            {
              id: 'uk.co.warwickartscentre.www.14287',
              source: 'arts-centre',
              start: '2017-05-24T14:00:00.000Z',
              end: '2017-05-24T14:00:00.000Z',
              isAllDay: false,
              title: 'Guardians of the Galaxy Vol.2 ',
              location: {
                name: 'Cinema'
              },
              href: 'https://www.warwickartscentre.co.uk/whats-on/2017/guardians-of-the-galaxy-vol.2-/'
            },
            {
              id: '20170524-8a17841b5bd80f4f015bd876d1c20733@warwick.ac.uk',
              source: 'insite',
              start: '2017-05-24T15:15:00.000Z',
              end: '2017-05-24T18:00:00.000Z',
              isAllDay: false,
              title: 'CAGE/WMG Post-Brexit Industrial Strategy: Fuel for the Midlands Engine?',
              location: null,
              href: 'http://www2.warwick.ac.uk/fac/soc/economics/research/centres/cage/events/24-05-17-cagewmg_post_brexit_industrial_strategy_fuel_for_the_midlands_engine'
            },
            {
              id: 'uk.co.warwickartscentre.www.13143',
              source: 'arts-centre',
              start: '2017-05-24T18:00:00.000Z',
              end: '2017-05-24T18:00:00.000Z',
              isAllDay: false,
              title: 'RSC Live: Antony & Cleopatra',
              location: {
                name: 'Cinema'
              },
              href: 'https://www.warwickartscentre.co.uk/whats-on/2017/rsc-live-antony-and-cleopatra/'
            },
            {
              id: 'uk.co.warwickartscentre.www.13128',
              source: 'arts-centre',
              start: '2017-05-24T18:30:00.000Z',
              end: '2017-05-24T18:30:00.000Z',
              isAllDay: false,
              title: 'Super Sunday',
              location: {
                name: 'Theatre'
              },
              href: 'https://www.warwickartscentre.co.uk/whats-on/2017/super-sunday/'
            },
            {
              id: 'uk.co.warwickartscentre.www.12291',
              source: 'arts-centre',
              start: '2017-05-24T18:30:00.000Z',
              end: '2017-05-24T18:30:00.000Z',
              isAllDay: false,
              title: 'Moscow Philharmonic Orchestra',
              location: {
                name: 'Butterworth Hall'
              },
              href: 'https://www.warwickartscentre.co.uk/whats-on/2017/moscow-philharmonic-orchestra/'
            },
            {
              id: 'uk.co.warwickartscentre.www.13646',
              source: 'arts-centre',
              start: '2017-05-24T18:45:00.000Z',
              end: '2017-05-24T18:45:00.000Z',
              isAllDay: false,
              title: 'TEN',
              location: {
                name: 'Studio'
              },
              href: 'https://www.warwickartscentre.co.uk/whats-on/2017/ten/'
            },
            {
              id: 'uk.co.warwickartscentre.www.13963',
              source: 'arts-centre',
              start: '2017-05-25T11:00:00.000Z',
              end: '2017-05-25T20:00:00.000Z',
              isAllDay: false,
              title: 'Room',
              location: {
                name: 'Mead Gallery'
              },
              href: 'https://www.warwickartscentre.co.uk/whats-on/2017/room-2/'
            },
            {
              id: '20170525-8a17841b5c0c782d015c15b2bc3a632b@warwick.ac.uk',
              source: 'insite',
              start: '2017-05-25T12:00:00.000Z',
              end: '2017-05-25T13:00:00.000Z',
              isAllDay: false,
              title: 'Academic governance review: Open meeting',
              location: null,
              href: 'http://www2.warwick.ac.uk/services/aro/acgovreview/openevent/'
            },
            {
              id: 'uk.co.warwickartscentre.www.13937',
              source: 'arts-centre',
              start: '2017-05-25T12:10:00.000Z',
              end: '2017-05-25T12:10:00.000Z',
              isAllDay: false,
              title: 'Lunchtime Concerts Summer 2017 ',
              location: {
                name: 'Ensemble Room'
              },
              href: 'https://www.warwickartscentre.co.uk/whats-on/2017/lunchtime-concerts-summer-2017-/'
            },
            {
              id: 'uk.co.warwickartscentre.www.14256',
              source: 'arts-centre',
              start: '2017-05-25T17:00:00.000Z',
              end: '2017-05-25T17:00:00.000Z',
              isAllDay: false,
              title: 'Cold Harbour ',
              location: {
                name: 'Cinema'
              },
              href: 'https://www.warwickartscentre.co.uk/whats-on/2017/cold-harbour-/'
            },
            {
              id: 'uk.co.warwickartscentre.www.13491',
              source: 'arts-centre',
              start: '2017-05-25T18:30:00.000Z',
              end: '2017-05-25T18:30:00.000Z',
              isAllDay: false,
              title: 'My Country; a work in progress',
              location: {
                name: 'Theatre'
              },
              href: 'https://www.warwickartscentre.co.uk/whats-on/2017/my-country-a-work-in-progress/'
            },
            {
              id: 'uk.co.warwickartscentre.www.14288',
              source: 'arts-centre',
              start: '2017-05-25T19:30:00.000Z',
              end: '2017-05-25T19:30:00.000Z',
              isAllDay: false,
              title: 'Guardians of the Galaxy Vol.2 ',
              location: {
                name: 'Cinema'
              },
              href: 'https://www.warwickartscentre.co.uk/whats-on/2017/guardians-of-the-galaxy-vol.2-/'
            },
            {
              id: 'uk.co.warwickartscentre.www.13964',
              source: 'arts-centre',
              start: '2017-05-26T11:00:00.000Z',
              end: '2017-05-26T20:00:00.000Z',
              isAllDay: false,
              title: 'Room',
              location: {
                name: 'Mead Gallery'
              },
              href: 'https://www.warwickartscentre.co.uk/whats-on/2017/room-2/'
            },
            {
              id: '20170526-8a17841b5b3d69e2015b8f3adc7d50ee@warwick.ac.uk',
              source: 'insite',
              start: '2017-05-26T12:00:00.000Z',
              end: '2017-05-26T13:00:00.000Z',
              isAllDay: false,
              title: 'Inaugural Elizabeth Creak Distinguised Guest lecture on Food Security',
              location: {
                name: 'GLT1, Gibbet Hill campus'
              }
            },
            {
              id: 'uk.co.warwickartscentre.www.13928',
              source: 'arts-centre',
              start: '2017-05-26T15:00:00.000Z',
              end: '2017-05-26T15:00:00.000Z',
              isAllDay: false,
              title: 'The Little Prince',
              location: {
                name: 'Helen Martin Studio'
              },
              href: 'https://www.warwickartscentre.co.uk/whats-on/2017/the-little-prince/'
            },
            {
              id: 'uk.co.warwickartscentre.www.14264',
              source: 'arts-centre',
              start: '2017-05-26T17:00:00.000Z',
              end: '2017-05-26T17:00:00.000Z',
              isAllDay: false,
              title: 'Alien: Covenant ',
              location: {
                name: 'Cinema'
              },
              href: 'https://www.warwickartscentre.co.uk/whats-on/2017/alien-covenant-/'
            },
            {
              id: 'uk.co.warwickartscentre.www.13492',
              source: 'arts-centre',
              start: '2017-05-26T18:30:00.000Z',
              end: '2017-05-26T18:30:00.000Z',
              isAllDay: false,
              title: 'My Country; a work in progress',
              location: {
                name: 'Theatre'
              },
              href: 'https://www.warwickartscentre.co.uk/whats-on/2017/my-country-a-work-in-progress/'
            },
            {
              id: 'uk.co.warwickartscentre.www.13682',
              source: 'arts-centre',
              start: '2017-05-26T18:30:00.000Z',
              end: '2017-05-26T18:30:00.000Z',
              isAllDay: false,
              title: 'Madeleine Peyroux ',
              location: {
                name: 'Butterworth Hall'
              },
              href: 'https://www.warwickartscentre.co.uk/whats-on/2017/madeleine-peyroux-/'
            },
            {
              id: 'uk.co.warwickartscentre.www.14257',
              source: 'arts-centre',
              start: '2017-05-26T19:30:00.000Z',
              end: '2017-05-26T19:30:00.000Z',
              isAllDay: false,
              title: 'The Zookeeper\'s Wife ',
              location: {
                name: 'Cinema'
              },
              href: 'https://www.warwickartscentre.co.uk/whats-on/2017/the-zookeepers-wife-/'
            },
            {
              id: 'uk.co.warwickartscentre.www.13965',
              source: 'arts-centre',
              start: '2017-05-27T11:00:00.000Z',
              end: '2017-05-27T20:00:00.000Z',
              isAllDay: false,
              title: 'Room',
              location: {
                name: 'Mead Gallery'
              },
              href: 'https://www.warwickartscentre.co.uk/whats-on/2017/room-2/'
            },
            {
              id: 'uk.co.warwickartscentre.www.13493',
              source: 'arts-centre',
              start: '2017-05-27T13:00:00.000Z',
              end: '2017-05-27T13:00:00.000Z',
              isAllDay: false,
              title: 'My Country; a work in progress',
              location: {
                name: 'Theatre'
              },
              href: 'https://www.warwickartscentre.co.uk/whats-on/2017/my-country-a-work-in-progress/'
            },
            {
              id: 'uk.co.warwickartscentre.www.14255',
              source: 'arts-centre',
              start: '2017-05-27T15:00:00.000Z',
              end: '2017-05-27T15:00:00.000Z',
              isAllDay: false,
              title: 'Mad to be Normal ',
              location: {
                name: 'Cinema'
              },
              href: 'https://www.warwickartscentre.co.uk/whats-on/2017/mad-to-be-normal-/'
            },
            {
              id: 'uk.co.warwickartscentre.www.14258',
              source: 'arts-centre',
              start: '2017-05-27T17:05:00.000Z',
              end: '2017-05-27T17:05:00.000Z',
              isAllDay: false,
              title: 'The Zookeeper\'s Wife ',
              location: {
                name: 'Cinema'
              },
              href: 'https://www.warwickartscentre.co.uk/whats-on/2017/the-zookeepers-wife-/'
            },
            {
              id: 'WSCFILMSCREENING@SCR_ID6407',
              source: 'warwick-student-cinema',
              start: '2017-05-27T17:30:00.000Z',
              isAllDay: false,
              title: 'John Wick: Chapter 2',
              location: {
                name: 'Lecture Theatre 3, (Science Concourse)'
              },
              href: 'https://warwick.film/filminfo?id=3485'
            },
            {
              id: 'uk.co.warwickartscentre.www.13494',
              source: 'arts-centre',
              start: '2017-05-27T18:30:00.000Z',
              end: '2017-05-27T18:30:00.000Z',
              isAllDay: false,
              title: 'My Country; a work in progress',
              location: {
                name: 'Theatre'
              },
              href: 'https://www.warwickartscentre.co.uk/whats-on/2017/my-country-a-work-in-progress/'
            },
            {
              id: 'uk.co.warwickartscentre.www.13397',
              source: 'arts-centre',
              start: '2017-05-27T18:30:00.000Z',
              end: '2017-05-27T18:30:00.000Z',
              isAllDay: false,
              title: ' KT Tunstall',
              location: {
                name: 'Butterworth Hall'
              },
              href: 'https://www.warwickartscentre.co.uk/whats-on/2017/kt-tunstall/'
            },
            {
              id: 'uk.co.warwickartscentre.www.13324',
              source: 'arts-centre',
              start: '2017-05-27T19:00:00.000Z',
              end: '2017-05-27T19:00:00.000Z',
              isAllDay: false,
              title: 'Stuart Goldsmith: Compared to What',
              location: {
                name: 'Studio'
              },
              href: 'https://www.warwickartscentre.co.uk/whats-on/2017/stuart-goldsmith-compared-to-what/'
            },
            {
              id: 'uk.co.warwickartscentre.www.14265',
              source: 'arts-centre',
              start: '2017-05-27T19:40:00.000Z',
              end: '2017-05-27T19:40:00.000Z',
              isAllDay: false,
              title: 'Alien: Covenant ',
              location: {
                name: 'Cinema'
              },
              href: 'https://www.warwickartscentre.co.uk/whats-on/2017/alien-covenant-/'
            },
            {
              id: 'WSCFILMSCREENING@SCR_ID6408',
              source: 'warwick-student-cinema',
              start: '2017-05-27T20:30:00.000Z',
              isAllDay: false,
              title: 'John Wick: Chapter 2',
              location: {
                name: 'Lecture Theatre 3, (Science Concourse)'
              },
              href: 'https://warwick.film/filminfo?id=3485'
            },
            {
              id: 'uk.co.warwickartscentre.www.13750',
              source: 'arts-centre',
              start: '2017-05-28T09:30:00.000Z',
              end: '2017-05-28T09:30:00.000Z',
              isAllDay: false,
              title: 'Dough! ',
              location: {
                name: 'Studio'
              },
              href: 'https://www.warwickartscentre.co.uk/whats-on/2017/dough-/'
            },
            {
              id: 'uk.co.warwickartscentre.www.13751',
              source: 'arts-centre',
              start: '2017-05-28T12:30:00.000Z',
              end: '2017-05-28T12:30:00.000Z',
              isAllDay: false,
              title: 'Dough! ',
              location: {
                name: 'Studio'
              },
              href: 'https://www.warwickartscentre.co.uk/whats-on/2017/dough-/'
            },
            {
              id: 'uk.co.warwickartscentre.www.14266',
              source: 'arts-centre',
              start: '2017-05-28T15:00:00.000Z',
              end: '2017-05-28T15:00:00.000Z',
              isAllDay: false,
              title: 'Alien: Covenant ',
              location: {
                name: 'Cinema'
              },
              href: 'https://www.warwickartscentre.co.uk/whats-on/2017/alien-covenant-/'
            },
            {
              id: 'uk.co.warwickartscentre.www.14138',
              source: 'arts-centre',
              start: '2017-05-28T18:30:00.000Z',
              end: '2017-05-28T18:30:00.000Z',
              isAllDay: false,
              title: 'The Hippopotamus ',
              location: {
                name: 'Cinema'
              },
              href: 'https://www.warwickartscentre.co.uk/whats-on/2017/the-hippopotamus-/'
            },
            {
              id: 'uk.co.warwickartscentre.www.13966',
              source: 'arts-centre',
              start: '2017-05-29T11:00:00.000Z',
              end: '2017-05-29T20:00:00.000Z',
              isAllDay: false,
              title: 'Room',
              location: {
                name: 'Mead Gallery'
              },
              href: 'https://www.warwickartscentre.co.uk/whats-on/2017/room-2/'
            },
            {
              id: 'uk.co.warwickartscentre.www.14259',
              source: 'arts-centre',
              start: '2017-05-29T15:00:00.000Z',
              end: '2017-05-29T15:00:00.000Z',
              isAllDay: false,
              title: 'The Zookeeper\'s Wife ',
              location: {
                name: 'Cinema'
              },
              href: 'https://www.warwickartscentre.co.uk/whats-on/2017/the-zookeepers-wife-/'
            },
            {
              id: 'uk.co.warwickartscentre.www.14267',
              source: 'arts-centre',
              start: '2017-05-29T18:30:00.000Z',
              end: '2017-05-29T18:30:00.000Z',
              isAllDay: false,
              title: 'Alien: Covenant ',
              location: {
                name: 'Cinema'
              },
              href: 'https://www.warwickartscentre.co.uk/whats-on/2017/alien-covenant-/'
            },
            {
              id: 'uk.co.warwickartscentre.www.13967',
              source: 'arts-centre',
              start: '2017-05-30T11:00:00.000Z',
              end: '2017-05-30T20:00:00.000Z',
              isAllDay: false,
              title: 'Room',
              location: {
                name: 'Mead Gallery'
              },
              href: 'https://www.warwickartscentre.co.uk/whats-on/2017/room-2/'
            },
            {
              id: 'uk.co.warwickartscentre.www.13946',
              source: 'arts-centre',
              start: '2017-05-30T11:00:00.000Z',
              end: '2017-05-30T20:00:00.000Z',
              isAllDay: false,
              title: 'Creative Week in the Mead Gallery: Summer 2017',
              location: {
                name: 'Mead Gallery'
              },
              href: 'https://www.warwickartscentre.co.uk/whats-on/2017/creative-week-in-the-mead-gallery-summer-2017/'
            },
            {
              id: 'uk.co.warwickartscentre.www.14260',
              source: 'arts-centre',
              start: '2017-05-30T17:00:00.000Z',
              end: '2017-05-30T17:00:00.000Z',
              isAllDay: false,
              title: 'The Zookeeper\'s Wife ',
              location: {
                name: 'Cinema'
              },
              href: 'https://www.warwickartscentre.co.uk/whats-on/2017/the-zookeepers-wife-/'
            },
            {
              id: 'uk.co.warwickartscentre.www.14268',
              source: 'arts-centre',
              start: '2017-05-30T19:40:00.000Z',
              end: '2017-05-30T19:40:00.000Z',
              isAllDay: false,
              title: 'Alien: Covenant ',
              location: {
                name: 'Cinema'
              },
              href: 'https://www.warwickartscentre.co.uk/whats-on/2017/alien-covenant-/'
            },
            {
              id: '20170531-094d43f559b249800159b75ad7e93680@warwick.ac.uk',
              source: 'insite',
              start: '2017-05-31T08:00:00.000Z',
              end: '2017-05-31T16:15:00.000Z',
              isAllDay: false,
              title: 'Higher Education Technicians Summit 2017 (HETS 2017)',
              location: {
                name: 'Oculus building'
              },
              href: 'http://www2.warwick.ac.uk/newsandevents/events/technicians/'
            },
            {
              id: 'uk.co.warwickartscentre.www.13949',
              source: 'arts-centre',
              start: '2017-05-31T09:00:00.000Z',
              end: '2017-05-31T11:00:00.000Z',
              isAllDay: false,
              title: 'Explore & Make with the Mead Gallery',
              location: {
                name: 'Mead Gallery'
              },
              href: 'https://www.warwickartscentre.co.uk/whats-on/2017/explore-and-make-with-the-mead-gallery/'
            },
            {
              id: 'uk.co.warwickartscentre.www.13968',
              source: 'arts-centre',
              start: '2017-05-31T11:00:00.000Z',
              end: '2017-05-31T20:00:00.000Z',
              isAllDay: false,
              title: 'Room',
              location: {
                name: 'Mead Gallery'
              },
              href: 'https://www.warwickartscentre.co.uk/whats-on/2017/room-2/'
            },
            {
              id: 'uk.co.warwickartscentre.www.13945',
              source: 'arts-centre',
              start: '2017-05-31T11:00:00.000Z',
              end: '2017-05-31T20:00:00.000Z',
              isAllDay: false,
              title: 'Creative Week in the Mead Gallery: Summer 2017',
              location: {
                name: 'Mead Gallery'
              },
              href: 'https://www.warwickartscentre.co.uk/whats-on/2017/creative-week-in-the-mead-gallery-summer-2017/'
            },
            {
              id: '20170531-8a17841a5c0c7b27015c15b7b0a93e23@warwick.ac.uk',
              source: 'insite',
              start: '2017-05-31T11:30:00.000Z',
              end: '2017-05-31T12:30:00.000Z',
              isAllDay: false,
              title: 'Collaborative design for teaching and learning: Design thinking techniques',
              location: null,
              href: 'http://www2.warwick.ac.uk/services/ldc/development/wow/design_thinking2'
            },
            {
              id: 'uk.co.warwickartscentre.www.14261',
              source: 'arts-centre',
              start: '2017-05-31T14:30:00.000Z',
              end: '2017-05-31T14:30:00.000Z',
              isAllDay: false,
              title: 'The Zookeeper\'s Wife ',
              location: {
                name: 'Cinema'
              },
              href: 'https://www.warwickartscentre.co.uk/whats-on/2017/the-zookeepers-wife-/'
            },
            {
              id: 'uk.co.warwickartscentre.www.14269',
              source: 'arts-centre',
              start: '2017-05-31T17:00:00.000Z',
              end: '2017-05-31T17:00:00.000Z',
              isAllDay: false,
              title: 'Alien: Covenant ',
              location: {
                name: 'Cinema'
              },
              href: 'https://www.warwickartscentre.co.uk/whats-on/2017/alien-covenant-/'
            },
            {
              id: 'uk.co.warwickartscentre.www.14388',
              source: 'arts-centre',
              start: '2017-05-31T18:45:00.000Z',
              end: '2017-05-31T18:45:00.000Z',
              isAllDay: false,
              title: 'WUDS Jerusalem',
              location: {
                name: 'Studio'
              },
              href: 'https://www.warwickartscentre.co.uk/whats-on/2017/wuds-jerusalem/'
            }
          ]
        },
        fetchedAt: 1495097974677,
        fetching: false
      },
      bus: {
        content: {
          items: [
            {
              id: '4a9c8b274d359759f82870fcf03461a9',
              callout: '10:07',
              text: '12X from Interchange to Pool Meadow Bus Station (Coventry)'
            },
            {
              id: '7358d957392efeaf85f9219538458f90',
              callout: '10:09',
              text: '12X from Gibbet Hill Campus to Pool Meadow Bus Station (Coventry)'
            },
            {
              id: '907de24797879f9b49d15319c6be2124',
              callout: '10:17',
              text: '11U from Interchange to Pool Meadow Bus Station (Coventry)'
            },
            {
              id: '89df32f6a3ed9dc4a23cc5dfc69c59cb',
              callout: '10:22',
              text: '12X from Interchange to Pool Meadow Bus Station (Coventry)'
            },
            {
              id: '5e067a13c78830b3ee570cf54908b4e1',
              callout: '10:24',
              text: '12X from Gibbet Hill Campus to Pool Meadow Bus Station (Coventry)'
            },
            {
              id: '3656eea336bbf6127c0d6366ae661f9b',
              callout: '10:37',
              text: '12X from Interchange to Pool Meadow Bus Station (Coventry)'
            },
            {
              id: 'e256cdc3a1584b0ccead91b1de9280e7',
              callout: '10:39',
              text: '12X from Gibbet Hill Campus to Pool Meadow Bus Station (Coventry)'
            },
            {
              id: 'af7313496e4bbd9f146af570eca1427a',
              callout: '10:47',
              text: '11U from Interchange to Pool Meadow Bus Station (Coventry)'
            },
            {
              id: '47bf0ff173c74e9a2e651e809d52cd8e',
              callout: '10:52',
              text: '12X from Interchange to Pool Meadow Bus Station (Coventry)'
            },
            {
              id: '649cda249a4b4ca333834908e1fea5a7',
              callout: '10:54',
              text: '12X from Gibbet Hill Campus to Pool Meadow Bus Station (Coventry)'
            },
            {
              id: 'a6114b67832e74bfc8b58f00095cdfb5',
              callout: '11:07',
              text: '12X from Interchange to Pool Meadow Bus Station (Coventry)'
            },
            {
              id: 'c4a4a4dbf2c11c074c3816f56a09076d',
              callout: '11:09',
              text: '12X from Gibbet Hill Campus to Pool Meadow Bus Station (Coventry)'
            },
            {
              id: '6549e849f495de3534775f7e45fef655',
              callout: '11:17',
              text: '11U from Interchange to Pool Meadow Bus Station (Coventry)'
            },
            {
              id: '7c2518cf42128651c66d9e65973de706',
              callout: '11:22',
              text: '12X from Interchange to Pool Meadow Bus Station (Coventry)'
            },
            {
              id: '4483fa81f3692c18c2a38a9ca864bf84',
              callout: '11:24',
              text: '12X from Gibbet Hill Campus to Pool Meadow Bus Station (Coventry)'
            },
            {
              id: '2dcf295a6aac72b091d40f3b40bce341',
              callout: '11:37',
              text: '12X from Interchange to Pool Meadow Bus Station (Coventry)'
            },
            {
              id: '6768e16d595d1756cf376fd4930af702',
              callout: '11:39',
              text: '12X from Gibbet Hill Campus to Pool Meadow Bus Station (Coventry)'
            },
            {
              id: '67175039bda054d4a99cce05023fc8f2',
              callout: '11:47',
              text: '11U from Interchange to Pool Meadow Bus Station (Coventry)'
            },
            {
              id: 'e998861566bb8081a6aee195ce34e70e',
              callout: '11:52',
              text: '12X from Interchange to Pool Meadow Bus Station (Coventry)'
            },
            {
              id: 'b078180099010f0e0f0d19e842579d4e',
              callout: '11:54',
              text: '12X from Gibbet Hill Campus to Pool Meadow Bus Station (Coventry)'
            }
          ]
        },
        fetchedAt: 1495097973815,
        fetching: false
      },
      coursework: {
        fetching: false,
        fetchedAt: 1495097978435,
        content: {
          defaultText: 'You are not enrolled on a course.',
          items: [
            {
              id: '6723',
              title: 'Test Mahara',
              text: 'Moodle Features Demo ()',
              href: 'http://moodle.warwick.ac.uk/mod/assign/view.php?id=294623',
              date: '2017-03-30T00:00:00+00:00'
            },
            {
              id: '4251',
              title: 'estream Video Assignment Demo',
              text: 'Moodle Features Demo ()',
              href: 'http://moodle.warwick.ac.uk/mod/assign/view.php?id=177391',
              date: '2017-11-03T00:00:00+00:00'
            },
            {
              id: '63',
              title: 'File Upload Assignment Example',
              text: 'Moodle Features Demo ()',
              href: 'http://moodle.warwick.ac.uk/mod/assign/view.php?id=3046',
              date: '2018-05-12T12:55:00+00:00'
            },
            {
              id: '6855',
              title: 'File Upload Assignment Example',
              text: 'Moodle Features Demo ()',
              href: 'http://moodle.warwick.ac.uk/mod/assign/view.php?id=302491',
              date: '2018-05-12T12:55:00+00:00'
            }
          ]
        }
      },
      eating: {
        content: {
          href: 'https://eating.warwick.ac.uk/uow/uow.aspx',
          items: [
            {
              id: 'balance',
              callout: '£5.15',
              text: 'on 17th May 2017 at 15:42'
            }
          ]
        },
        fetchedAt: 1495097973864,
        fetching: false
      },
      print: {
        content: {
          href: 'https://printercredits.warwick.ac.uk',
          items: [
            {
              id: 'balance',
              callout: '£50050.01',
              text: 'credit available'
            }
          ]
        },
        fetchedAt: 1495097973744,
        fetching: false
      },
      weather: {
        content: {
          minutelySummary: 'Clear for the hour.',
          hourlySummary: 'Mostly cloudy starting this afternoon.',
          currentConditions: {
            time: 1495097965,
            summary: 'Clear',
            icon: 'clear-day',
            temperature: 10.93
          },
          items: [
            {
              id: 1495094400,
              time: 1495094400,
              summary: 'Clear',
              icon: 'clear-day',
              temperature: 10.68,
              precipProbability: 0
            },
            {
              id: 1495098000,
              time: 1495098000,
              summary: 'Clear',
              icon: 'clear-day',
              temperature: 10.93,
              precipProbability: 0
            },
            {
              id: 1495101600,
              time: 1495101600,
              summary: 'Clear',
              icon: 'clear-day',
              temperature: 12.23,
              precipProbability: 0
            },
            {
              id: 1495105200,
              time: 1495105200,
              summary: 'Clear',
              icon: 'clear-day',
              temperature: 13.62,
              precipProbability: 0.01
            },
            {
              id: 1495108800,
              time: 1495108800,
              summary: 'Clear',
              icon: 'clear-day',
              temperature: 15.12,
              precipProbability: 0.03
            },
            {
              id: 1495112400,
              time: 1495112400,
              summary: 'Partly Cloudy',
              icon: 'partly-cloudy-day',
              temperature: 15.82,
              precipProbability: 0.06
            }
          ]
        },
        fetchedAt: 1495097973781,
        fetching: false
      },
      timetable: {
        content: {
          defaultText: 'You have no events in the next 30 days.',
          items: []
        },
        fetchedAt: 1495097974060,
        fetching: false
      },
      traffic: {
        content: {
          items: [
            {
              route: {
                name: 'A45 to Campus',
                inbound: true,
                start: {
                  latitude: 52.390173,
                  longitude: -1.555059
                },
                end: {
                  latitude: 52.379436,
                  longitude: -1.560668
                }
              },
              summary: 'Charter Ave',
              usualDuration: {
                text: '5 mins',
                value: 309
              },
              actualDuration: {
                text: '5 mins',
                value: 313
              }
            },
            {
              route: {
                name: 'A46 to Campus',
                inbound: true,
                start: {
                  latitude: 52.358556,
                  longitude: -1.531676
                },
                end: {
                  latitude: 52.379436,
                  longitude: -1.560668
                }
              },
              summary: 'Stoneleigh Rd and Gibbet Hill Rd',
              usualDuration: {
                text: '5 mins',
                value: 308
              },
              actualDuration: {
                text: '5 mins',
                value: 325
              }
            },
            {
              route: {
                name: 'Campus to A45',
                inbound: false,
                start: {
                  latitude: 52.390173,
                  longitude: -1.555059
                },
                end: {
                  latitude: 52.379436,
                  longitude: -1.560668
                }
              },
              summary: 'Charter Ave',
              usualDuration: {
                text: '5 mins',
                value: 309
              },
              actualDuration: {
                text: '5 mins',
                value: 313
              }
            },
            {
              route: {
                name: 'Campus to A46',
                inbound: false,
                start: {
                  latitude: 52.379436,
                  longitude: -1.560668
                },
                end: {
                  latitude: 52.358556,
                  longitude: -1.531676
                }
              },
              summary: 'Gibbet Hill Rd and Stoneleigh Rd',
              usualDuration: {
                text: '5 mins',
                value: 291
              },
              actualDuration: {
                text: '5 mins',
                value: 300
              }
            }
          ],
          alerts: {
            href: 'http://www2.warwick.ac.uk/services/estates/news/?tag=Roads%20and%20parking&tag=Live%20Update',
            items: []
          }
        },
        fetchedAt: 1495097974079,
        fetching: false
      },
      library: {
        content: {
          subtitle: '1 loan',
          items: [
            {
              id: 'https://pugwash.lib.warwick.ac.uk/iii/sierra-api/v3/patrons/checkouts/149446',
              itemTitle: 'Enterprise database connectivity : the key to enterprise applications on the desktop',
              dueMessage: 'Due in 47 days',
              type: 'loan'
            }
          ]
        },
        fetchedAt: 1495097974048,
        fetching: false
      },
      mail: {
        content: {
          defaultText: 'No unread messages',
          count: 3923,
          items: [
            {
              id: 'AAMkAGIxZTk0M2ZlLTMzYTItNGQxOC1iOTg5LTA3NDU5Y2JhMDZmMQBGAAAAAAAWHgPRQ_yIRrbcS4nzb9ZiBwANwPyQgb5HT5-MoH8I-LcPAAAAAAEMAAANwPyQgb5HT5-MoH8I-LcPAACXJRLAAAA=',
              title: 'University of Warwick Account Notifications',
              date: '2017-05-17T15:39:46.000Z',
              text: 'A trusted device has been added to your account',
              href: 'https://outlook.office365.com/owa/?ItemID=AAMkAGIxZTk0M2ZlLTMzYTItNGQxOC1iOTg5LTA3NDU5Y2JhMDZmMQBGAAAAAAAWHgPRQ%2ByIRrbcS4nzb9ZiBwANwPyQgb5HT5%2FMoH8I%2FLcPAAAAAAEMAAANwPyQgb5HT5%2FMoH8I%2FLcPAACXJRLAAAA%3D&exvsurl=1&viewmodel=ReadMessageItem'
            },
            {
              id: 'AAMkAGIxZTk0M2ZlLTMzYTItNGQxOC1iOTg5LTA3NDU5Y2JhMDZmMQBGAAAAAAAWHgPRQ_yIRrbcS4nzb9ZiBwANwPyQgb5HT5-MoH8I-LcPAAAAAAEMAAANwPyQgb5HT5-MoH8I-LcPAACXJRK9AAA=',
              title: 'University of Warwick Account Notifications',
              date: '2017-05-17T14:06:55.000Z',
              text: 'A trusted device has been added to your account',
              href: 'https://outlook.office365.com/owa/?ItemID=AAMkAGIxZTk0M2ZlLTMzYTItNGQxOC1iOTg5LTA3NDU5Y2JhMDZmMQBGAAAAAAAWHgPRQ%2ByIRrbcS4nzb9ZiBwANwPyQgb5HT5%2FMoH8I%2FLcPAAAAAAEMAAANwPyQgb5HT5%2FMoH8I%2FLcPAACXJRK9AAA%3D&exvsurl=1&viewmodel=ReadMessageItem'
            },
            {
              id: 'AAMkAGIxZTk0M2ZlLTMzYTItNGQxOC1iOTg5LTA3NDU5Y2JhMDZmMQBGAAAAAAAWHgPRQ_yIRrbcS4nzb9ZiBwANwPyQgb5HT5-MoH8I-LcPAAAAAAEMAAANwPyQgb5HT5-MoH8I-LcPAACXJRK8AAA=',
              title: 'University of Warwick Account Notifications',
              date: '2017-05-17T14:06:43.000Z',
              text: 'Important account security information',
              href: 'https://outlook.office365.com/owa/?ItemID=AAMkAGIxZTk0M2ZlLTMzYTItNGQxOC1iOTg5LTA3NDU5Y2JhMDZmMQBGAAAAAAAWHgPRQ%2ByIRrbcS4nzb9ZiBwANwPyQgb5HT5%2FMoH8I%2FLcPAAAAAAEMAAANwPyQgb5HT5%2FMoH8I%2FLcPAACXJRK8AAA%3D&exvsurl=1&viewmodel=ReadMessageItem'
            },
            {
              id: 'AAMkAGIxZTk0M2ZlLTMzYTItNGQxOC1iOTg5LTA3NDU5Y2JhMDZmMQBGAAAAAAAWHgPRQ_yIRrbcS4nzb9ZiBwANwPyQgb5HT5-MoH8I-LcPAAAAAAEMAAANwPyQgb5HT5-MoH8I-LcPAACXJRK7AAA=',
              title: 'University of Warwick Account Notifications',
              date: '2017-05-17T12:24:14.000Z',
              text: 'Important account security information',
              href: 'https://outlook.office365.com/owa/?ItemID=AAMkAGIxZTk0M2ZlLTMzYTItNGQxOC1iOTg5LTA3NDU5Y2JhMDZmMQBGAAAAAAAWHgPRQ%2ByIRrbcS4nzb9ZiBwANwPyQgb5HT5%2FMoH8I%2FLcPAAAAAAEMAAANwPyQgb5HT5%2FMoH8I%2FLcPAACXJRK7AAA%3D&exvsurl=1&viewmodel=ReadMessageItem'
            },
            {
              id: 'AAMkAGIxZTk0M2ZlLTMzYTItNGQxOC1iOTg5LTA3NDU5Y2JhMDZmMQBGAAAAAAAWHgPRQ_yIRrbcS4nzb9ZiBwANwPyQgb5HT5-MoH8I-LcPAAAAAAEMAAANwPyQgb5HT5-MoH8I-LcPAACXJRK6AAA=',
              title: 'Atlassian JIRA',
              date: '2017-05-16T17:16:18.000Z',
              text: 'JIRA Insiders: May 2017',
              href: 'https://outlook.office365.com/owa/?ItemID=AAMkAGIxZTk0M2ZlLTMzYTItNGQxOC1iOTg5LTA3NDU5Y2JhMDZmMQBGAAAAAAAWHgPRQ%2ByIRrbcS4nzb9ZiBwANwPyQgb5HT5%2FMoH8I%2FLcPAAAAAAEMAAANwPyQgb5HT5%2FMoH8I%2FLcPAACXJRK6AAA%3D&exvsurl=1&viewmodel=ReadMessageItem'
            },
            {
              id: 'AAMkAGIxZTk0M2ZlLTMzYTItNGQxOC1iOTg5LTA3NDU5Y2JhMDZmMQBGAAAAAAAWHgPRQ_yIRrbcS4nzb9ZiBwANwPyQgb5HT5-MoH8I-LcPAAAAAAEMAAANwPyQgb5HT5-MoH8I-LcPAACXJRK5AAA=',
              title: 'inbox insite: Staff newsletter',
              date: '2017-05-16T16:45:17.000Z',
              text: 'University Awards winners, new research leadership roles, pay and reward, governance review, join our open day team',
              href: 'https://outlook.office365.com/owa/?ItemID=AAMkAGIxZTk0M2ZlLTMzYTItNGQxOC1iOTg5LTA3NDU5Y2JhMDZmMQBGAAAAAAAWHgPRQ%2ByIRrbcS4nzb9ZiBwANwPyQgb5HT5%2FMoH8I%2FLcPAAAAAAEMAAANwPyQgb5HT5%2FMoH8I%2FLcPAACXJRK5AAA%3D&exvsurl=1&viewmodel=ReadMessageItem'
            },
            {
              id: 'AAMkAGIxZTk0M2ZlLTMzYTItNGQxOC1iOTg5LTA3NDU5Y2JhMDZmMQBGAAAAAAAWHgPRQ_yIRrbcS4nzb9ZiBwANwPyQgb5HT5-MoH8I-LcPAAAAAAEMAAANwPyQgb5HT5-MoH8I-LcPAACXJRK3AAA=',
              title: 'University of Warwick Account Notifications',
              date: '2017-05-16T13:51:58.000Z',
              text: 'A trusted device has been added to your account',
              href: 'https://outlook.office365.com/owa/?ItemID=AAMkAGIxZTk0M2ZlLTMzYTItNGQxOC1iOTg5LTA3NDU5Y2JhMDZmMQBGAAAAAAAWHgPRQ%2ByIRrbcS4nzb9ZiBwANwPyQgb5HT5%2FMoH8I%2FLcPAAAAAAEMAAANwPyQgb5HT5%2FMoH8I%2FLcPAACXJRK3AAA%3D&exvsurl=1&viewmodel=ReadMessageItem'
            },
            {
              id: 'AAMkAGIxZTk0M2ZlLTMzYTItNGQxOC1iOTg5LTA3NDU5Y2JhMDZmMQBGAAAAAAAWHgPRQ_yIRrbcS4nzb9ZiBwANwPyQgb5HT5-MoH8I-LcPAAAAAAEMAAANwPyQgb5HT5-MoH8I-LcPAACXJRKjAAA=',
              title: 'Mat Mannion (JIRA)',
              date: '2017-05-15T10:45:36.000Z',
              text: '[JIRA] Resolved: (TAB-4911) Investigate why https://sandbox.turnitin.com returns 401 for all jobs.',
              href: 'https://outlook.office365.com/owa/?ItemID=AAMkAGIxZTk0M2ZlLTMzYTItNGQxOC1iOTg5LTA3NDU5Y2JhMDZmMQBGAAAAAAAWHgPRQ%2ByIRrbcS4nzb9ZiBwANwPyQgb5HT5%2FMoH8I%2FLcPAAAAAAEMAAANwPyQgb5HT5%2FMoH8I%2FLcPAACXJRKjAAA%3D&exvsurl=1&viewmodel=ReadMessageItem'
            },
            {
              id: 'AAMkAGIxZTk0M2ZlLTMzYTItNGQxOC1iOTg5LTA3NDU5Y2JhMDZmMQBGAAAAAAAWHgPRQ_yIRrbcS4nzb9ZiBwANwPyQgb5HT5-MoH8I-LcPAAAAAAEMAAANwPyQgb5HT5-MoH8I-LcPAACXJRKfAAA=',
              title: 'Sara Lever (JIRA)',
              date: '2017-05-15T10:26:21.000Z',
              text: '[JIRA] Updated: (TAB-4881) Tabula Home page improvements',
              href: 'https://outlook.office365.com/owa/?ItemID=AAMkAGIxZTk0M2ZlLTMzYTItNGQxOC1iOTg5LTA3NDU5Y2JhMDZmMQBGAAAAAAAWHgPRQ%2ByIRrbcS4nzb9ZiBwANwPyQgb5HT5%2FMoH8I%2FLcPAAAAAAEMAAANwPyQgb5HT5%2FMoH8I%2FLcPAACXJRKfAAA%3D&exvsurl=1&viewmodel=ReadMessageItem'
            },
            {
              id: 'AAMkAGIxZTk0M2ZlLTMzYTItNGQxOC1iOTg5LTA3NDU5Y2JhMDZmMQBGAAAAAAAWHgPRQ_yIRrbcS4nzb9ZiBwANwPyQgb5HT5-MoH8I-LcPAAAAAAEMAAANwPyQgb5HT5-MoH8I-LcPAACXJRKeAAA=',
              title: 'Sara Lever (JIRA)',
              date: '2017-05-15T10:24:42.000Z',
              text: '[JIRA] Updated: (TAB-4799) User testing to see what use cases do Tabula personas attempt on mobiles do these work?',
              href: 'https://outlook.office365.com/owa/?ItemID=AAMkAGIxZTk0M2ZlLTMzYTItNGQxOC1iOTg5LTA3NDU5Y2JhMDZmMQBGAAAAAAAWHgPRQ%2ByIRrbcS4nzb9ZiBwANwPyQgb5HT5%2FMoH8I%2FLcPAAAAAAEMAAANwPyQgb5HT5%2FMoH8I%2FLcPAACXJRKeAAA%3D&exvsurl=1&viewmodel=ReadMessageItem'
            },
            {
              id: 'AAMkAGIxZTk0M2ZlLTMzYTItNGQxOC1iOTg5LTA3NDU5Y2JhMDZmMQBGAAAAAAAWHgPRQ_yIRrbcS4nzb9ZiBwANwPyQgb5HT5-MoH8I-LcPAAAAAAEMAAANwPyQgb5HT5-MoH8I-LcPAACXJRKZAAA=',
              title: 'Matthew Jones (JIRA)',
              date: '2017-05-15T09:55:17.000Z',
              text: '[JIRA] Resolved: (TAB-5092) Change monitoring points to \'attended\'',
              href: 'https://outlook.office365.com/owa/?ItemID=AAMkAGIxZTk0M2ZlLTMzYTItNGQxOC1iOTg5LTA3NDU5Y2JhMDZmMQBGAAAAAAAWHgPRQ%2ByIRrbcS4nzb9ZiBwANwPyQgb5HT5%2FMoH8I%2FLcPAAAAAAEMAAANwPyQgb5HT5%2FMoH8I%2FLcPAACXJRKZAAA%3D&exvsurl=1&viewmodel=ReadMessageItem'
            },
            {
              id: 'AAMkAGIxZTk0M2ZlLTMzYTItNGQxOC1iOTg5LTA3NDU5Y2JhMDZmMQBGAAAAAAAWHgPRQ_yIRrbcS4nzb9ZiBwANwPyQgb5HT5-MoH8I-LcPAAAAAAEMAAANwPyQgb5HT5-MoH8I-LcPAACXJRKUAAA=',
              title: 'Mat Mannion (JIRA)',
              date: '2017-05-15T09:43:30.000Z',
              text: '[JIRA] Resolved: (SBTWO-7892) Deleting more than 200 old form submissions in one go - PG Study',
              href: 'https://outlook.office365.com/owa/?ItemID=AAMkAGIxZTk0M2ZlLTMzYTItNGQxOC1iOTg5LTA3NDU5Y2JhMDZmMQBGAAAAAAAWHgPRQ%2ByIRrbcS4nzb9ZiBwANwPyQgb5HT5%2FMoH8I%2FLcPAAAAAAEMAAANwPyQgb5HT5%2FMoH8I%2FLcPAACXJRKUAAA%3D&exvsurl=1&viewmodel=ReadMessageItem'
            },
            {
              id: 'AAMkAGIxZTk0M2ZlLTMzYTItNGQxOC1iOTg5LTA3NDU5Y2JhMDZmMQBGAAAAAAAWHgPRQ_yIRrbcS4nzb9ZiBwANwPyQgb5HT5-MoH8I-LcPAAAAAAEMAAANwPyQgb5HT5-MoH8I-LcPAACXJRKTAAA=',
              title: 'Matthew Jones (JIRA)',
              date: '2017-05-15T09:41:27.000Z',
              text: '[JIRA] Assigned: (TAB-5092) Change monitoring points to \'attended\'',
              href: 'https://outlook.office365.com/owa/?ItemID=AAMkAGIxZTk0M2ZlLTMzYTItNGQxOC1iOTg5LTA3NDU5Y2JhMDZmMQBGAAAAAAAWHgPRQ%2ByIRrbcS4nzb9ZiBwANwPyQgb5HT5%2FMoH8I%2FLcPAAAAAAEMAAANwPyQgb5HT5%2FMoH8I%2FLcPAACXJRKTAAA%3D&exvsurl=1&viewmodel=ReadMessageItem'
            },
            {
              id: 'AAMkAGIxZTk0M2ZlLTMzYTItNGQxOC1iOTg5LTA3NDU5Y2JhMDZmMQBGAAAAAAAWHgPRQ_yIRrbcS4nzb9ZiBwANwPyQgb5HT5-MoH8I-LcPAAAAAAEMAAANwPyQgb5HT5-MoH8I-LcPAACXJRKSAAA=',
              title: 'Julie Moreton (JIRA)',
              date: '2017-05-15T09:35:26.000Z',
              text: '[JIRA] Commented: (SBTWO-7892) Deleting more than 200 old form submissions in one go - PG Study',
              href: 'https://outlook.office365.com/owa/?ItemID=AAMkAGIxZTk0M2ZlLTMzYTItNGQxOC1iOTg5LTA3NDU5Y2JhMDZmMQBGAAAAAAAWHgPRQ%2ByIRrbcS4nzb9ZiBwANwPyQgb5HT5%2FMoH8I%2FLcPAAAAAAEMAAANwPyQgb5HT5%2FMoH8I%2FLcPAACXJRKSAAA%3D&exvsurl=1&viewmodel=ReadMessageItem'
            },
            {
              id: 'AAMkAGIxZTk0M2ZlLTMzYTItNGQxOC1iOTg5LTA3NDU5Y2JhMDZmMQBGAAAAAAAWHgPRQ_yIRrbcS4nzb9ZiBwANwPyQgb5HT5-MoH8I-LcPAAAAAAEMAAANwPyQgb5HT5-MoH8I-LcPAACXJRKOAAA=',
              title: 'Mat Mannion (JIRA)',
              date: '2017-05-15T09:31:15.000Z',
              text: '[JIRA] Resolved: (SBTWO-7893) Add admin permissions for hosfdz on /services/aro/dar/quality/ and subsites',
              href: 'https://outlook.office365.com/owa/?ItemID=AAMkAGIxZTk0M2ZlLTMzYTItNGQxOC1iOTg5LTA3NDU5Y2JhMDZmMQBGAAAAAAAWHgPRQ%2ByIRrbcS4nzb9ZiBwANwPyQgb5HT5%2FMoH8I%2FLcPAAAAAAEMAAANwPyQgb5HT5%2FMoH8I%2FLcPAACXJRKOAAA%3D&exvsurl=1&viewmodel=ReadMessageItem'
            },
            {
              id: 'AAMkAGIxZTk0M2ZlLTMzYTItNGQxOC1iOTg5LTA3NDU5Y2JhMDZmMQBGAAAAAAAWHgPRQ_yIRrbcS4nzb9ZiBwANwPyQgb5HT5-MoH8I-LcPAAAAAAEMAAANwPyQgb5HT5-MoH8I-LcPAACXJRKNAAA=',
              title: 'Mat Mannion (JIRA)',
              date: '2017-05-15T09:28:16.000Z',
              text: '[JIRA] Assigned: (SBTWO-7893) Add admin permissions for hosfdz on /services/aro/dar/quality/ and subsites',
              href: 'https://outlook.office365.com/owa/?ItemID=AAMkAGIxZTk0M2ZlLTMzYTItNGQxOC1iOTg5LTA3NDU5Y2JhMDZmMQBGAAAAAAAWHgPRQ%2ByIRrbcS4nzb9ZiBwANwPyQgb5HT5%2FMoH8I%2FLcPAAAAAAEMAAANwPyQgb5HT5%2FMoH8I%2FLcPAACXJRKNAAA%3D&exvsurl=1&viewmodel=ReadMessageItem'
            },
            {
              id: 'AAMkAGIxZTk0M2ZlLTMzYTItNGQxOC1iOTg5LTA3NDU5Y2JhMDZmMQBGAAAAAAAWHgPRQ_yIRrbcS4nzb9ZiBwANwPyQgb5HT5-MoH8I-LcPAAAAAAEMAAANwPyQgb5HT5-MoH8I-LcPAACQY-y_AAA=',
              title: 'Mat Mannion (JIRA)',
              date: '2017-05-12T14:58:29.000Z',
              text: '[JIRA] Resolved: (TAB-5103) Member API',
              href: 'https://outlook.office365.com/owa/?ItemID=AAMkAGIxZTk0M2ZlLTMzYTItNGQxOC1iOTg5LTA3NDU5Y2JhMDZmMQBGAAAAAAAWHgPRQ%2ByIRrbcS4nzb9ZiBwANwPyQgb5HT5%2FMoH8I%2FLcPAAAAAAEMAAANwPyQgb5HT5%2FMoH8I%2FLcPAACQY%2Fy%2BAAA%3D&exvsurl=1&viewmodel=ReadMessageItem'
            },
            {
              id: 'AAMkAGIxZTk0M2ZlLTMzYTItNGQxOC1iOTg5LTA3NDU5Y2JhMDZmMQBGAAAAAAAWHgPRQ_yIRrbcS4nzb9ZiBwANwPyQgb5HT5-MoH8I-LcPAAAAAAEMAAANwPyQgb5HT5-MoH8I-LcPAACQY-y8AAA=',
              title: 'Mat Mannion (JIRA)',
              date: '2017-05-12T14:57:14.000Z',
              text: '[JIRA] Resolved: (TAB-5106) API documentation for Member API',
              href: 'https://outlook.office365.com/owa/?ItemID=AAMkAGIxZTk0M2ZlLTMzYTItNGQxOC1iOTg5LTA3NDU5Y2JhMDZmMQBGAAAAAAAWHgPRQ%2ByIRrbcS4nzb9ZiBwANwPyQgb5HT5%2FMoH8I%2FLcPAAAAAAEMAAANwPyQgb5HT5%2FMoH8I%2FLcPAACQY%2Fy8AAA%3D&exvsurl=1&viewmodel=ReadMessageItem'
            },
            {
              id: 'AAMkAGIxZTk0M2ZlLTMzYTItNGQxOC1iOTg5LTA3NDU5Y2JhMDZmMQBGAAAAAAAWHgPRQ_yIRrbcS4nzb9ZiBwANwPyQgb5HT5-MoH8I-LcPAAAAAAEMAAANwPyQgb5HT5-MoH8I-LcPAACQY-yyAAA=',
              title: 'Ritchie Allen (JIRA)',
              date: '2017-05-12T13:44:16.000Z',
              text: '[JIRA] Resolved: (TAB-5105) Allow Economics to use Markdown to allow html in generic feedback',
              href: 'https://outlook.office365.com/owa/?ItemID=AAMkAGIxZTk0M2ZlLTMzYTItNGQxOC1iOTg5LTA3NDU5Y2JhMDZmMQBGAAAAAAAWHgPRQ%2ByIRrbcS4nzb9ZiBwANwPyQgb5HT5%2FMoH8I%2FLcPAAAAAAEMAAANwPyQgb5HT5%2FMoH8I%2FLcPAACQY%2FyyAAA%3D&exvsurl=1&viewmodel=ReadMessageItem'
            },
            {
              id: 'AAMkAGIxZTk0M2ZlLTMzYTItNGQxOC1iOTg5LTA3NDU5Y2JhMDZmMQBGAAAAAAAWHgPRQ_yIRrbcS4nzb9ZiBwANwPyQgb5HT5-MoH8I-LcPAAAAAAEMAAANwPyQgb5HT5-MoH8I-LcPAACQY-yxAAA=',
              title: 'Julie Moreton (JIRA)',
              date: '2017-05-12T13:42:42.000Z',
              text: '[JIRA] Created: (TAB-5105) Allow Economics to use Markdown to allow html in generic feedback',
              href: 'https://outlook.office365.com/owa/?ItemID=AAMkAGIxZTk0M2ZlLTMzYTItNGQxOC1iOTg5LTA3NDU5Y2JhMDZmMQBGAAAAAAAWHgPRQ%2ByIRrbcS4nzb9ZiBwANwPyQgb5HT5%2FMoH8I%2FLcPAAAAAAEMAAANwPyQgb5HT5%2FMoH8I%2FLcPAACQY%2FyxAAA%3D&exvsurl=1&viewmodel=ReadMessageItem'
            },
            {
              id: 'AAMkAGIxZTk0M2ZlLTMzYTItNGQxOC1iOTg5LTA3NDU5Y2JhMDZmMQBGAAAAAAAWHgPRQ_yIRrbcS4nzb9ZiBwANwPyQgb5HT5-MoH8I-LcPAAAAAAEMAAANwPyQgb5HT5-MoH8I-LcPAACQY-ywAAA=',
              title: 'Ritchie Allen (JIRA)',
              date: '2017-05-12T13:40:53.000Z',
              text: '[JIRA] Resolved: (TAB-5104) Move route X352 from under IE to under CE (CLL)',
              href: 'https://outlook.office365.com/owa/?ItemID=AAMkAGIxZTk0M2ZlLTMzYTItNGQxOC1iOTg5LTA3NDU5Y2JhMDZmMQBGAAAAAAAWHgPRQ%2ByIRrbcS4nzb9ZiBwANwPyQgb5HT5%2FMoH8I%2FLcPAAAAAAEMAAANwPyQgb5HT5%2FMoH8I%2FLcPAACQY%2FywAAA%3D&exvsurl=1&viewmodel=ReadMessageItem'
            },
            {
              id: 'AAMkAGIxZTk0M2ZlLTMzYTItNGQxOC1iOTg5LTA3NDU5Y2JhMDZmMQBGAAAAAAAWHgPRQ_yIRrbcS4nzb9ZiBwANwPyQgb5HT5-MoH8I-LcPAAAAAAEMAAANwPyQgb5HT5-MoH8I-LcPAACQY-yvAAA=',
              title: 'Ritchie Allen (JIRA)',
              date: '2017-05-12T13:39:36.000Z',
              text: '[JIRA] Commented: (TAB-5104) Move route X352 from under IE to under CE (CLL)',
              href: 'https://outlook.office365.com/owa/?ItemID=AAMkAGIxZTk0M2ZlLTMzYTItNGQxOC1iOTg5LTA3NDU5Y2JhMDZmMQBGAAAAAAAWHgPRQ%2ByIRrbcS4nzb9ZiBwANwPyQgb5HT5%2FMoH8I%2FLcPAAAAAAEMAAANwPyQgb5HT5%2FMoH8I%2FLcPAACQY%2FyvAAA%3D&exvsurl=1&viewmodel=ReadMessageItem'
            },
            {
              id: 'AAMkAGIxZTk0M2ZlLTMzYTItNGQxOC1iOTg5LTA3NDU5Y2JhMDZmMQBGAAAAAAAWHgPRQ_yIRrbcS4nzb9ZiBwANwPyQgb5HT5-MoH8I-LcPAAAAAAEMAAANwPyQgb5HT5-MoH8I-LcPAACQY-yuAAA=',
              title: 'Karen Mortimer (JIRA)',
              date: '2017-05-12T13:37:57.000Z',
              text: '[JIRA] Updated: (TAB-5104) Move route X352 from under IE to under CE (CLL)',
              href: 'https://outlook.office365.com/owa/?ItemID=AAMkAGIxZTk0M2ZlLTMzYTItNGQxOC1iOTg5LTA3NDU5Y2JhMDZmMQBGAAAAAAAWHgPRQ%2ByIRrbcS4nzb9ZiBwANwPyQgb5HT5%2FMoH8I%2FLcPAAAAAAEMAAANwPyQgb5HT5%2FMoH8I%2FLcPAACQY%2FyuAAA%3D&exvsurl=1&viewmodel=ReadMessageItem'
            },
            {
              id: 'AAMkAGIxZTk0M2ZlLTMzYTItNGQxOC1iOTg5LTA3NDU5Y2JhMDZmMQBGAAAAAAAWHgPRQ_yIRrbcS4nzb9ZiBwANwPyQgb5HT5-MoH8I-LcPAAAAAAEMAAANwPyQgb5HT5-MoH8I-LcPAACQY-ytAAA=',
              title: 'Ritchie Allen (JIRA)',
              date: '2017-05-12T13:37:47.000Z',
              text: '[JIRA] Assigned: (TAB-5104) Move route X352 from under IE to under CE (CLL)',
              href: 'https://outlook.office365.com/owa/?ItemID=AAMkAGIxZTk0M2ZlLTMzYTItNGQxOC1iOTg5LTA3NDU5Y2JhMDZmMQBGAAAAAAAWHgPRQ%2ByIRrbcS4nzb9ZiBwANwPyQgb5HT5%2FMoH8I%2FLcPAAAAAAEMAAANwPyQgb5HT5%2FMoH8I%2FLcPAACQY%2FytAAA%3D&exvsurl=1&viewmodel=ReadMessageItem'
            },
            {
              id: 'AAMkAGIxZTk0M2ZlLTMzYTItNGQxOC1iOTg5LTA3NDU5Y2JhMDZmMQBGAAAAAAAWHgPRQ_yIRrbcS4nzb9ZiBwANwPyQgb5HT5-MoH8I-LcPAAAAAAEMAAANwPyQgb5HT5-MoH8I-LcPAACQY-ysAAA=',
              title: 'Karen Mortimer (JIRA)',
              date: '2017-05-12T13:36:23.000Z',
              text: '[JIRA] Created: (TAB-5104) Move route X352 from under IE to under CE (CLL)',
              href: 'https://outlook.office365.com/owa/?ItemID=AAMkAGIxZTk0M2ZlLTMzYTItNGQxOC1iOTg5LTA3NDU5Y2JhMDZmMQBGAAAAAAAWHgPRQ%2ByIRrbcS4nzb9ZiBwANwPyQgb5HT5%2FMoH8I%2FLcPAAAAAAEMAAANwPyQgb5HT5%2FMoH8I%2FLcPAACQY%2FysAAA%3D&exvsurl=1&viewmodel=ReadMessageItem'
            },
            {
              id: 'AAMkAGIxZTk0M2ZlLTMzYTItNGQxOC1iOTg5LTA3NDU5Y2JhMDZmMQBGAAAAAAAWHgPRQ_yIRrbcS4nzb9ZiBwANwPyQgb5HT5-MoH8I-LcPAAAAAAEMAAANwPyQgb5HT5-MoH8I-LcPAACQY-ypAAA=',
              title: 'Mat Mannion (JIRA)',
              date: '2017-05-12T13:05:55.000Z',
              text: '[JIRA] Commented: (TAB-5103) Member API',
              href: 'https://outlook.office365.com/owa/?ItemID=AAMkAGIxZTk0M2ZlLTMzYTItNGQxOC1iOTg5LTA3NDU5Y2JhMDZmMQBGAAAAAAAWHgPRQ%2ByIRrbcS4nzb9ZiBwANwPyQgb5HT5%2FMoH8I%2FLcPAAAAAAEMAAANwPyQgb5HT5%2FMoH8I%2FLcPAACQY%2FypAAA%3D&exvsurl=1&viewmodel=ReadMessageItem'
            },
            {
              id: 'AAMkAGIxZTk0M2ZlLTMzYTItNGQxOC1iOTg5LTA3NDU5Y2JhMDZmMQBGAAAAAAAWHgPRQ_yIRrbcS4nzb9ZiBwANwPyQgb5HT5-MoH8I-LcPAAAAAAEMAAANwPyQgb5HT5-MoH8I-LcPAACQY-yoAAA=',
              title: 'Mat Mannion (JIRA)',
              date: '2017-05-12T13:03:23.000Z',
              text: '[JIRA] Created: (TAB-5103) Member API',
              href: 'https://outlook.office365.com/owa/?ItemID=AAMkAGIxZTk0M2ZlLTMzYTItNGQxOC1iOTg5LTA3NDU5Y2JhMDZmMQBGAAAAAAAWHgPRQ%2ByIRrbcS4nzb9ZiBwANwPyQgb5HT5%2FMoH8I%2FLcPAAAAAAEMAAANwPyQgb5HT5%2FMoH8I%2FLcPAACQY%2FyoAAA%3D&exvsurl=1&viewmodel=ReadMessageItem'
            },
            {
              id: 'AAMkAGIxZTk0M2ZlLTMzYTItNGQxOC1iOTg5LTA3NDU5Y2JhMDZmMQBGAAAAAAAWHgPRQ_yIRrbcS4nzb9ZiBwANwPyQgb5HT5-MoH8I-LcPAAAAAAEMAAANwPyQgb5HT5-MoH8I-LcPAACQY-ynAAA=',
              title: 'Mat Mannion (JIRA)',
              date: '2017-05-12T13:00:06.000Z',
              text: '[JIRA] Assigned: (TAB-5103) Member API',
              href: 'https://outlook.office365.com/owa/?ItemID=AAMkAGIxZTk0M2ZlLTMzYTItNGQxOC1iOTg5LTA3NDU5Y2JhMDZmMQBGAAAAAAAWHgPRQ%2ByIRrbcS4nzb9ZiBwANwPyQgb5HT5%2FMoH8I%2FLcPAAAAAAEMAAANwPyQgb5HT5%2FMoH8I%2FLcPAACQY%2FynAAA%3D&exvsurl=1&viewmodel=ReadMessageItem'
            },
            {
              id: 'AAMkAGIxZTk0M2ZlLTMzYTItNGQxOC1iOTg5LTA3NDU5Y2JhMDZmMQBGAAAAAAAWHgPRQ_yIRrbcS4nzb9ZiBwANwPyQgb5HT5-MoH8I-LcPAAAAAAEMAAANwPyQgb5HT5-MoH8I-LcPAACQY-ylAAA=',
              title: 'Matthew Jones (JIRA)',
              date: '2017-05-12T11:58:38.000Z',
              text: '[JIRA] Resolved: (TAB-5097) Update uploaded monitoring point data for 0027108',
              href: 'https://outlook.office365.com/owa/?ItemID=AAMkAGIxZTk0M2ZlLTMzYTItNGQxOC1iOTg5LTA3NDU5Y2JhMDZmMQBGAAAAAAAWHgPRQ%2ByIRrbcS4nzb9ZiBwANwPyQgb5HT5%2FMoH8I%2FLcPAAAAAAEMAAANwPyQgb5HT5%2FMoH8I%2FLcPAACQY%2FylAAA%3D&exvsurl=1&viewmodel=ReadMessageItem'
            },
            {
              id: 'AAMkAGIxZTk0M2ZlLTMzYTItNGQxOC1iOTg5LTA3NDU5Y2JhMDZmMQBGAAAAAAAWHgPRQ_yIRrbcS4nzb9ZiBwANwPyQgb5HT5-MoH8I-LcPAAAAAAEMAAANwPyQgb5HT5-MoH8I-LcPAACQY-ykAAA=',
              title: 'Matthew Jones (JIRA)',
              date: '2017-05-12T11:58:16.000Z',
              text: '[JIRA] Assigned: (TAB-5097) Update uploaded monitoring point data for 0027108',
              href: 'https://outlook.office365.com/owa/?ItemID=AAMkAGIxZTk0M2ZlLTMzYTItNGQxOC1iOTg5LTA3NDU5Y2JhMDZmMQBGAAAAAAAWHgPRQ%2ByIRrbcS4nzb9ZiBwANwPyQgb5HT5%2FMoH8I%2FLcPAAAAAAEMAAANwPyQgb5HT5%2FMoH8I%2FLcPAACQY%2FykAAA%3D&exvsurl=1&viewmodel=ReadMessageItem'
            },
            {
              id: 'AAMkAGIxZTk0M2ZlLTMzYTItNGQxOC1iOTg5LTA3NDU5Y2JhMDZmMQBGAAAAAAAWHgPRQ_yIRrbcS4nzb9ZiBwANwPyQgb5HT5-MoH8I-LcPAAAAAAEMAAANwPyQgb5HT5-MoH8I-LcPAACQY-yjAAA=',
              title: 'Mat Mannion (JIRA)',
              date: '2017-05-12T10:31:21.000Z',
              text: '[JIRA] Resolved: (TAB-4211) Student popup within the Student Profile list popup Esc key not working',
              href: 'https://outlook.office365.com/owa/?ItemID=AAMkAGIxZTk0M2ZlLTMzYTItNGQxOC1iOTg5LTA3NDU5Y2JhMDZmMQBGAAAAAAAWHgPRQ%2ByIRrbcS4nzb9ZiBwANwPyQgb5HT5%2FMoH8I%2FLcPAAAAAAEMAAANwPyQgb5HT5%2FMoH8I%2FLcPAACQY%2FyjAAA%3D&exvsurl=1&viewmodel=ReadMessageItem'
            },
            {
              id: 'AAMkAGIxZTk0M2ZlLTMzYTItNGQxOC1iOTg5LTA3NDU5Y2JhMDZmMQBGAAAAAAAWHgPRQ_yIRrbcS4nzb9ZiBwANwPyQgb5HT5-MoH8I-LcPAAAAAAEMAAANwPyQgb5HT5-MoH8I-LcPAACQY-yiAAA=',
              title: 'Karen Mortimer (JIRA)',
              date: '2017-05-12T10:23:35.000Z',
              text: '[JIRA] Updated: (SBTWO-7749) Formsbuilder attachment upload stalls in Edge',
              href: 'https://outlook.office365.com/owa/?ItemID=AAMkAGIxZTk0M2ZlLTMzYTItNGQxOC1iOTg5LTA3NDU5Y2JhMDZmMQBGAAAAAAAWHgPRQ%2ByIRrbcS4nzb9ZiBwANwPyQgb5HT5%2FMoH8I%2FLcPAAAAAAEMAAANwPyQgb5HT5%2FMoH8I%2FLcPAACQY%2FyiAAA%3D&exvsurl=1&viewmodel=ReadMessageItem'
            },
            {
              id: 'AAMkAGIxZTk0M2ZlLTMzYTItNGQxOC1iOTg5LTA3NDU5Y2JhMDZmMQBGAAAAAAAWHgPRQ_yIRrbcS4nzb9ZiBwANwPyQgb5HT5-MoH8I-LcPAAAAAAEMAAANwPyQgb5HT5-MoH8I-LcPAACQY-yhAAA=',
              title: 'Mat Mannion (JIRA)',
              date: '2017-05-12T10:23:18.000Z',
              text: '[JIRA] Resolved: (TAB-4210) Student Profile list popup prevents scrolling after viewing a student profile popup',
              href: 'https://outlook.office365.com/owa/?ItemID=AAMkAGIxZTk0M2ZlLTMzYTItNGQxOC1iOTg5LTA3NDU5Y2JhMDZmMQBGAAAAAAAWHgPRQ%2ByIRrbcS4nzb9ZiBwANwPyQgb5HT5%2FMoH8I%2FLcPAAAAAAEMAAANwPyQgb5HT5%2FMoH8I%2FLcPAACQY%2FyhAAA%3D&exvsurl=1&viewmodel=ReadMessageItem'
            },
            {
              id: 'AAMkAGIxZTk0M2ZlLTMzYTItNGQxOC1iOTg5LTA3NDU5Y2JhMDZmMQBGAAAAAAAWHgPRQ_yIRrbcS4nzb9ZiBwANwPyQgb5HT5-MoH8I-LcPAAAAAAEMAAANwPyQgb5HT5-MoH8I-LcPAACQY-ygAAA=',
              title: 'Mat Mannion (JIRA)',
              date: '2017-05-12T09:56:12.000Z',
              text: '[JIRA] Assigned: (TAB-4210) Student Profile list popup prevents scrolling after viewing a student profile popup',
              href: 'https://outlook.office365.com/owa/?ItemID=AAMkAGIxZTk0M2ZlLTMzYTItNGQxOC1iOTg5LTA3NDU5Y2JhMDZmMQBGAAAAAAAWHgPRQ%2ByIRrbcS4nzb9ZiBwANwPyQgb5HT5%2FMoH8I%2FLcPAAAAAAEMAAANwPyQgb5HT5%2FMoH8I%2FLcPAACQY%2FygAAA%3D&exvsurl=1&viewmodel=ReadMessageItem'
            },
            {
              id: 'AAMkAGIxZTk0M2ZlLTMzYTItNGQxOC1iOTg5LTA3NDU5Y2JhMDZmMQBGAAAAAAAWHgPRQ_yIRrbcS4nzb9ZiBwANwPyQgb5HT5-MoH8I-LcPAAAAAAEMAAANwPyQgb5HT5-MoH8I-LcPAACQY-yfAAA=',
              title: 'Mat Mannion (JIRA)',
              date: '2017-05-12T09:52:29.000Z',
              text: '[JIRA] Resolved: (TAB-4594) Lecture not always included as an Event type filter option on department timetables',
              href: 'https://outlook.office365.com/owa/?ItemID=AAMkAGIxZTk0M2ZlLTMzYTItNGQxOC1iOTg5LTA3NDU5Y2JhMDZmMQBGAAAAAAAWHgPRQ%2ByIRrbcS4nzb9ZiBwANwPyQgb5HT5%2FMoH8I%2FLcPAAAAAAEMAAANwPyQgb5HT5%2FMoH8I%2FLcPAACQY%2FyfAAA%3D&exvsurl=1&viewmodel=ReadMessageItem'
            },
            {
              id: 'AAMkAGIxZTk0M2ZlLTMzYTItNGQxOC1iOTg5LTA3NDU5Y2JhMDZmMQBGAAAAAAAWHgPRQ_yIRrbcS4nzb9ZiBwANwPyQgb5HT5-MoH8I-LcPAAAAAAEMAAANwPyQgb5HT5-MoH8I-LcPAACQY-yeAAA=',
              title: 'Mat Mannion (JIRA)',
              date: '2017-05-12T09:44:28.000Z',
              text: '[JIRA] Commented: (EXAMS-84) Send audit logs to CLogS',
              href: 'https://outlook.office365.com/owa/?ItemID=AAMkAGIxZTk0M2ZlLTMzYTItNGQxOC1iOTg5LTA3NDU5Y2JhMDZmMQBGAAAAAAAWHgPRQ%2ByIRrbcS4nzb9ZiBwANwPyQgb5HT5%2FMoH8I%2FLcPAAAAAAEMAAANwPyQgb5HT5%2FMoH8I%2FLcPAACQY%2FyeAAA%3D&exvsurl=1&viewmodel=ReadMessageItem'
            },
            {
              id: 'AAMkAGIxZTk0M2ZlLTMzYTItNGQxOC1iOTg5LTA3NDU5Y2JhMDZmMQBGAAAAAAAWHgPRQ_yIRrbcS4nzb9ZiBwANwPyQgb5HT5-MoH8I-LcPAAAAAAEMAAANwPyQgb5HT5-MoH8I-LcPAACQY-ydAAA=',
              title: 'Mat Mannion (JIRA)',
              date: '2017-05-12T09:20:54.000Z',
              text: '[JIRA] Resolved: (TAB-3865) API: Support submissions without a university ID',
              href: 'https://outlook.office365.com/owa/?ItemID=AAMkAGIxZTk0M2ZlLTMzYTItNGQxOC1iOTg5LTA3NDU5Y2JhMDZmMQBGAAAAAAAWHgPRQ%2ByIRrbcS4nzb9ZiBwANwPyQgb5HT5%2FMoH8I%2FLcPAAAAAAEMAAANwPyQgb5HT5%2FMoH8I%2FLcPAACQY%2FydAAA%3D&exvsurl=1&viewmodel=ReadMessageItem'
            },
            {
              id: 'AAMkAGIxZTk0M2ZlLTMzYTItNGQxOC1iOTg5LTA3NDU5Y2JhMDZmMQBGAAAAAAAWHgPRQ_yIRrbcS4nzb9ZiBwANwPyQgb5HT5-MoH8I-LcPAAAAAAEMAAANwPyQgb5HT5-MoH8I-LcPAACQY-ybAAA=',
              title: 'Mat Mannion (JIRA)',
              date: '2017-05-12T09:19:12.000Z',
              text: '[JIRA] Assigned: (TAB-3865) API: Support submissions without a university ID',
              href: 'https://outlook.office365.com/owa/?ItemID=AAMkAGIxZTk0M2ZlLTMzYTItNGQxOC1iOTg5LTA3NDU5Y2JhMDZmMQBGAAAAAAAWHgPRQ%2ByIRrbcS4nzb9ZiBwANwPyQgb5HT5%2FMoH8I%2FLcPAAAAAAEMAAANwPyQgb5HT5%2FMoH8I%2FLcPAACQY%2FybAAA%3D&exvsurl=1&viewmodel=ReadMessageItem'
            },
            {
              id: 'AAMkAGIxZTk0M2ZlLTMzYTItNGQxOC1iOTg5LTA3NDU5Y2JhMDZmMQBGAAAAAAAWHgPRQ_yIRrbcS4nzb9ZiBwANwPyQgb5HT5-MoH8I-LcPAAAAAAEMAAANwPyQgb5HT5-MoH8I-LcPAACQY-yGAAA=',
              title: 'Sara Lever (JIRA)',
              date: '2017-05-11T15:46:00.000Z',
              text: '[JIRA] Updated: (TAB-4851) Display detailed audit trail of workflow actions to admins (and markers?)',
              href: 'https://outlook.office365.com/owa/?ItemID=AAMkAGIxZTk0M2ZlLTMzYTItNGQxOC1iOTg5LTA3NDU5Y2JhMDZmMQBGAAAAAAAWHgPRQ%2ByIRrbcS4nzb9ZiBwANwPyQgb5HT5%2FMoH8I%2FLcPAAAAAAEMAAANwPyQgb5HT5%2FMoH8I%2FLcPAACQY%2FyGAAA%3D&exvsurl=1&viewmodel=ReadMessageItem'
            },
            {
              id: 'AAMkAGIxZTk0M2ZlLTMzYTItNGQxOC1iOTg5LTA3NDU5Y2JhMDZmMQBGAAAAAAAWHgPRQ_yIRrbcS4nzb9ZiBwANwPyQgb5HT5-MoH8I-LcPAAAAAAEMAAANwPyQgb5HT5-MoH8I-LcPAACQY-yEAAA=',
              title: 'Ritchie Allen (JIRA)',
              date: '2017-05-11T15:37:42.000Z',
              text: '[JIRA] Resolved: (TAB-5100) TransientObjectException in MarkingCompletedCommand',
              href: 'https://outlook.office365.com/owa/?ItemID=AAMkAGIxZTk0M2ZlLTMzYTItNGQxOC1iOTg5LTA3NDU5Y2JhMDZmMQBGAAAAAAAWHgPRQ%2ByIRrbcS4nzb9ZiBwANwPyQgb5HT5%2FMoH8I%2FLcPAAAAAAEMAAANwPyQgb5HT5%2FMoH8I%2FLcPAACQY%2FyEAAA%3D&exvsurl=1&viewmodel=ReadMessageItem'
            },
            {
              id: 'AAMkAGIxZTk0M2ZlLTMzYTItNGQxOC1iOTg5LTA3NDU5Y2JhMDZmMQBGAAAAAAAWHgPRQ_yIRrbcS4nzb9ZiBwANwPyQgb5HT5-MoH8I-LcPAAAAAAEMAAANwPyQgb5HT5-MoH8I-LcPAACQY-yDAAA=',
              title: 'Sara Lever (JIRA)',
              date: '2017-05-11T15:35:48.000Z',
              text: '[JIRA] Updated: (TAB-3098) Automatically release late submissions',
              href: 'https://outlook.office365.com/owa/?ItemID=AAMkAGIxZTk0M2ZlLTMzYTItNGQxOC1iOTg5LTA3NDU5Y2JhMDZmMQBGAAAAAAAWHgPRQ%2ByIRrbcS4nzb9ZiBwANwPyQgb5HT5%2FMoH8I%2FLcPAAAAAAEMAAANwPyQgb5HT5%2FMoH8I%2FLcPAACQY%2FyDAAA%3D&exvsurl=1&viewmodel=ReadMessageItem'
            },
            {
              id: 'AAMkAGIxZTk0M2ZlLTMzYTItNGQxOC1iOTg5LTA3NDU5Y2JhMDZmMQBGAAAAAAAWHgPRQ_yIRrbcS4nzb9ZiBwANwPyQgb5HT5-MoH8I-LcPAAAAAAEMAAANwPyQgb5HT5-MoH8I-LcPAACQY-yBAAA=',
              title: 'Mat Mannion (JIRA)',
              date: '2017-05-11T15:32:46.000Z',
              text: '[JIRA] Resolved: (TAB-5102) Allow Markdown in formatted HTML fields',
              href: 'https://outlook.office365.com/owa/?ItemID=AAMkAGIxZTk0M2ZlLTMzYTItNGQxOC1iOTg5LTA3NDU5Y2JhMDZmMQBGAAAAAAAWHgPRQ%2ByIRrbcS4nzb9ZiBwANwPyQgb5HT5%2FMoH8I%2FLcPAAAAAAEMAAANwPyQgb5HT5%2FMoH8I%2FLcPAACQY%2FyBAAA%3D&exvsurl=1&viewmodel=ReadMessageItem'
            },
            {
              id: 'AAMkAGIxZTk0M2ZlLTMzYTItNGQxOC1iOTg5LTA3NDU5Y2JhMDZmMQBGAAAAAAAWHgPRQ_yIRrbcS4nzb9ZiBwANwPyQgb5HT5-MoH8I-LcPAAAAAAEMAAANwPyQgb5HT5-MoH8I-LcPAACQY-x6AAA=',
              title: 'Julie Moreton (JIRA)',
              date: '2017-05-11T15:04:29.000Z',
              text: '[JIRA] Updated: (TAB-5100) TransientObjectException in MarkingCompletedCommand',
              href: 'https://outlook.office365.com/owa/?ItemID=AAMkAGIxZTk0M2ZlLTMzYTItNGQxOC1iOTg5LTA3NDU5Y2JhMDZmMQBGAAAAAAAWHgPRQ%2ByIRrbcS4nzb9ZiBwANwPyQgb5HT5%2FMoH8I%2FLcPAAAAAAEMAAANwPyQgb5HT5%2FMoH8I%2FLcPAACQY%2Fx6AAA%3D&exvsurl=1&viewmodel=ReadMessageItem'
            },
            {
              id: 'AAMkAGIxZTk0M2ZlLTMzYTItNGQxOC1iOTg5LTA3NDU5Y2JhMDZmMQBGAAAAAAAWHgPRQ_yIRrbcS4nzb9ZiBwANwPyQgb5HT5-MoH8I-LcPAAAAAAEMAAANwPyQgb5HT5-MoH8I-LcPAACQY-x5AAA=',
              title: 'Ritchie Allen (JIRA)',
              date: '2017-05-11T14:52:45.000Z',
              text: '[JIRA] Commented: (TAB-5100) TransientObjectException in MarkingCompletedCommand',
              href: 'https://outlook.office365.com/owa/?ItemID=AAMkAGIxZTk0M2ZlLTMzYTItNGQxOC1iOTg5LTA3NDU5Y2JhMDZmMQBGAAAAAAAWHgPRQ%2ByIRrbcS4nzb9ZiBwANwPyQgb5HT5%2FMoH8I%2FLcPAAAAAAEMAAANwPyQgb5HT5%2FMoH8I%2FLcPAACQY%2Fx5AAA%3D&exvsurl=1&viewmodel=ReadMessageItem'
            },
            {
              id: 'AAMkAGIxZTk0M2ZlLTMzYTItNGQxOC1iOTg5LTA3NDU5Y2JhMDZmMQBGAAAAAAAWHgPRQ_yIRrbcS4nzb9ZiBwANwPyQgb5HT5-MoH8I-LcPAAAAAAEMAAANwPyQgb5HT5-MoH8I-LcPAACQY-x3AAA=',
              title: 'Ritchie Allen (JIRA)',
              date: '2017-05-11T14:50:05.000Z',
              text: '[JIRA] Reopened: (TAB-5100) TransientObjectException in MarkingCompletedCommand',
              href: 'https://outlook.office365.com/owa/?ItemID=AAMkAGIxZTk0M2ZlLTMzYTItNGQxOC1iOTg5LTA3NDU5Y2JhMDZmMQBGAAAAAAAWHgPRQ%2ByIRrbcS4nzb9ZiBwANwPyQgb5HT5%2FMoH8I%2FLcPAAAAAAEMAAANwPyQgb5HT5%2FMoH8I%2FLcPAACQY%2Fx3AAA%3D&exvsurl=1&viewmodel=ReadMessageItem'
            },
            {
              id: 'AAMkAGIxZTk0M2ZlLTMzYTItNGQxOC1iOTg5LTA3NDU5Y2JhMDZmMQBGAAAAAAAWHgPRQ_yIRrbcS4nzb9ZiBwANwPyQgb5HT5-MoH8I-LcPAAAAAAEMAAANwPyQgb5HT5-MoH8I-LcPAACQY-x1AAA=',
              title: 'Simon Harper (JIRA)',
              date: '2017-05-11T14:47:44.000Z',
              text: '[JIRA] Updated: (TAB-4461) Changes to Coursework Management workflow types',
              href: 'https://outlook.office365.com/owa/?ItemID=AAMkAGIxZTk0M2ZlLTMzYTItNGQxOC1iOTg5LTA3NDU5Y2JhMDZmMQBGAAAAAAAWHgPRQ%2ByIRrbcS4nzb9ZiBwANwPyQgb5HT5%2FMoH8I%2FLcPAAAAAAEMAAANwPyQgb5HT5%2FMoH8I%2FLcPAACQY%2Fx1AAA%3D&exvsurl=1&viewmodel=ReadMessageItem'
            },
            {
              id: 'AAMkAGIxZTk0M2ZlLTMzYTItNGQxOC1iOTg5LTA3NDU5Y2JhMDZmMQBGAAAAAAAWHgPRQ_yIRrbcS4nzb9ZiBwANwPyQgb5HT5-MoH8I-LcPAAAAAAEMAAANwPyQgb5HT5-MoH8I-LcPAACQY-x0AAA=',
              title: 'Sara Lever (JIRA)',
              date: '2017-05-11T14:47:28.000Z',
              text: '[JIRA] Commented: (TAB-4322)  Uploading generic feedback for coursework if there is no marking workflow set',
              href: 'https://outlook.office365.com/owa/?ItemID=AAMkAGIxZTk0M2ZlLTMzYTItNGQxOC1iOTg5LTA3NDU5Y2JhMDZmMQBGAAAAAAAWHgPRQ%2ByIRrbcS4nzb9ZiBwANwPyQgb5HT5%2FMoH8I%2FLcPAAAAAAEMAAANwPyQgb5HT5%2FMoH8I%2FLcPAACQY%2Fx0AAA%3D&exvsurl=1&viewmodel=ReadMessageItem'
            },
            {
              id: 'AAMkAGIxZTk0M2ZlLTMzYTItNGQxOC1iOTg5LTA3NDU5Y2JhMDZmMQBGAAAAAAAWHgPRQ_yIRrbcS4nzb9ZiBwANwPyQgb5HT5-MoH8I-LcPAAAAAAEMAAANwPyQgb5HT5-MoH8I-LcPAACQY-xzAAA=',
              title: 'Sara Lever (JIRA)',
              date: '2017-05-11T14:45:23.000Z',
              text: '[JIRA] Updated: (TAB-5015) Add Generic Feedback',
              href: 'https://outlook.office365.com/owa/?ItemID=AAMkAGIxZTk0M2ZlLTMzYTItNGQxOC1iOTg5LTA3NDU5Y2JhMDZmMQBGAAAAAAAWHgPRQ%2ByIRrbcS4nzb9ZiBwANwPyQgb5HT5%2FMoH8I%2FLcPAAAAAAEMAAANwPyQgb5HT5%2FMoH8I%2FLcPAACQY%2FxzAAA%3D&exvsurl=1&viewmodel=ReadMessageItem'
            },
            {
              id: 'AAMkAGIxZTk0M2ZlLTMzYTItNGQxOC1iOTg5LTA3NDU5Y2JhMDZmMQBGAAAAAAAWHgPRQ_yIRrbcS4nzb9ZiBwANwPyQgb5HT5-MoH8I-LcPAAAAAAEMAAANwPyQgb5HT5-MoH8I-LcPAACQY-xxAAA=',
              title: 'Stephen Woodward (JIRA)',
              date: '2017-05-11T14:44:24.000Z',
              text: '[JIRA] Resolved: (SBTWO-7896) Empty linkpicker when linking from an existing link in IE11',
              href: 'https://outlook.office365.com/owa/?ItemID=AAMkAGIxZTk0M2ZlLTMzYTItNGQxOC1iOTg5LTA3NDU5Y2JhMDZmMQBGAAAAAAAWHgPRQ%2ByIRrbcS4nzb9ZiBwANwPyQgb5HT5%2FMoH8I%2FLcPAAAAAAEMAAANwPyQgb5HT5%2FMoH8I%2FLcPAACQY%2FxxAAA%3D&exvsurl=1&viewmodel=ReadMessageItem'
            },
            {
              id: 'AAMkAGIxZTk0M2ZlLTMzYTItNGQxOC1iOTg5LTA3NDU5Y2JhMDZmMQBGAAAAAAAWHgPRQ_yIRrbcS4nzb9ZiBwANwPyQgb5HT5-MoH8I-LcPAAAAAAEMAAANwPyQgb5HT5-MoH8I-LcPAACQY-xwAAA=',
              title: 'Simon Harper (JIRA)',
              date: '2017-05-11T14:42:53.000Z',
              text: '[JIRA] Commented: (TAB-4461) Changes to Coursework Management workflow types',
              href: 'https://outlook.office365.com/owa/?ItemID=AAMkAGIxZTk0M2ZlLTMzYTItNGQxOC1iOTg5LTA3NDU5Y2JhMDZmMQBGAAAAAAAWHgPRQ%2ByIRrbcS4nzb9ZiBwANwPyQgb5HT5%2FMoH8I%2FLcPAAAAAAEMAAANwPyQgb5HT5%2FMoH8I%2FLcPAACQY%2FxwAAA%3D&exvsurl=1&viewmodel=ReadMessageItem'
            }
          ]
        },
        fetchedAt: 1495097974697,
        fetching: false
      },
      calendar: {
        content: {
          items: [
            {
              id: 'AAMkAGIxZTk0M2ZlLTMzYTItNGQxOC1iOTg5LTA3NDU5Y2JhMDZmMQFRAAgI1KFup9BAAEYAAAAAFh4D0UPsiEa23EuJ82-WYgcADcD8kIG_R0_fzKB-CPy3DwAAAAABDQAADcD8kIG_R0_fzKB-CPy3DwAAfTXmrQAAEA==',
              start: '2017-05-23T10:00:00.000Z',
              end: '2017-05-23T10:30:00.000Z',
              isAllDay: false,
              title: 'My Warwick',
              location: {
                name: 'Level 4'
              },
              href: 'https://outlook.office365.com/owa/?itemid=AAMkAGIxZTk0M2ZlLTMzYTItNGQxOC1iOTg5LTA3NDU5Y2JhMDZmMQFRAAgI1KFup9BAAEYAAAAAFh4D0UPsiEa23EuJ82%2FWYgcADcD8kIG%2BR0%2BfzKB%2FCPy3DwAAAAABDQAADcD8kIG%2BR0%2BfzKB%2FCPy3DwAAfTXmrQAAEA%3D%3D&exvsurl=1&path=/calendar/item',
              organiser: {
                name: 'Nick Howes',
                email: 'N.Howes@warwick.ac.uk'
              }
            }
          ]
        },
        fetchedAt: 1495097974467,
        fetching: false
      }
    },
    update: {
      isUpdateReady: false
    },
    user: {
      data: {
        authenticated: true,
        usercode: 'u1574595',
        analytics: {
          identifier: 'eef182c3e222eccb2f98f9db1a3cddc768cc0fac3112c6a21b11312560f53053',
          dimensions: [
            {
              index: 1,
              value: 'IT Services'
            },
            {
              index: 2,
              value: 'Staff'
            },
            {
              index: 3,
              value: null
            },
            {
              index: 4,
              value: null
            }
          ]
        },
        name: 'Kai Lan',
        masquerading: false,
        photo: {
          url: 'https://photos-dev.warwick.ac.uk/photo/54f740a2-5cfb-4630-963a-401f119669cf/60'
        }
      },
      authoritative: true,
      empty: false,
      links: {
        login: 'https://websignon.warwick.ac.uk/origin/hs?shire=https%3A%2F%2Fmy-dev.warwick.ac.uk%2Fsso%2Facs&providerId=urn%3Amy-dev.warwick.ac.uk%3Amywarwick%3Aservice&target=https%3A%2F%2Fmy-dev.warwick.ac.uk&status=permdenied',
        logout: 'https://my-dev.warwick.ac.uk/logout?target=https://my-dev.warwick.ac.uk'
      }
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
    analytics: {
      clientId: '1841291800.1495039340'
    },
    routing: {
      locationBeforeTransitions: {
        pathname: '/',
        search: '',
        hash: '',
        state: null,
        action: 'POP',
        key: 'rfvgwz',
        query: {},
        $searchBase: {
          search: '',
          searchBase: ''
        }
      }
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

  it('should now render unknown tiles or removed tiles', () => {
    const wrapper = shallow(<MeView.WrappedComponent {...props} />);
    expect(wrapper.find(TileView)).to.have.length(10);
  });

});
