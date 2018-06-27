import WeatherTile, { oneWordWeather } from 'components/tiles/WeatherTile';
import { localMomentUnix } from 'dateFormats';

describe('WeatherTile', () => {

  const data = {
      icon: 'clear-day',
      summary: 'Clear',
      temperature: 4.13,
      precipProbability: 0.55,
    },
    props = {
      'content': {
        minutelySummary: 'Partly cloudy for the hour',
        hourlySummary: 'Partly cloudy until this afternoon',
        currentConditions: { time: 1456316008, ...data },
        items: [
          { id: 1456316008, time: 1456316008, ...data },
          { id: 1456318800, time: 1456318800, ...data },
          { id: 1456322400, time: 1456322400, ...data },
          { id: 1456326000, time: 1456326000, ...data },
          { id: 1456329600, time: 1456329600, ...data },
          { id: 1456333200, time: 1456333200, ...data },
        ],
      },
      size: 'small',
    };

  it('displays a single weather item when size small', () => {
    const {
      props: {
        children: [
          calloutComponent,
          {
            props: {
              children: caption,
            },
          },
        ],
      },
    } = renderAtMoment(<WeatherTile { ...props } />);

    const {
      props: {
        className: calloutClassName,
        children: [
          degrees,
          degreesSymbol,
        ],
      },
    } = renderAtMoment(calloutComponent);

    calloutClassName.should.equal('tile__callout');
    degrees.should.equal(4);
    degreesSymbol.should.equal('°');

    caption.should.equal('Partly cloudy for the hour');
  });

  function testWideLayout(theseProps) {
    const {
      props: {
        children: [
          {
            props: {
              children: [
                {
                  props: {
                    children: calloutComponent,
                  },
                },
                {
                  props: {
                    children: caption,
                  }
                },
              ],
            },
          },
          weatherTable,
        ]
      }
    } = renderAtMoment(<WeatherTile { ...theseProps } />);

    const {
      props: {
        className: calloutClassName,
      },
    } = renderAtMoment(calloutComponent);
    calloutClassName.should.equal('tile__callout');

    caption.should.equal('Partly cloudy for the hour');

    const {
      props: {
        children: weatherTableItems,
      },
    } = renderAtMoment(weatherTable);
    weatherTableItems.length.should.equal(6);

    const [
      {
        props: {
          children: [
            {
              props: {
                children: time,
              },
            },
            {
              props: {
                children: condition,
              }
            },
            {
              props: {
                children: [ /* icon */ , /* space */ , precipProbability, percentSymbol ],
              },
            },
          ],
        },
      },
    ] = weatherTableItems;

    time.should.equal('12pm');
    condition.should.equal('clear');
    precipProbability.should.equal(55);
    percentSymbol.should.equal('%');
  }

  it('displays wide layout when wide', () => {
    testWideLayout({...props, size: 'wide' });
  });

  it('displays wide layout when zoomed', () => {
    testWideLayout({...props, zoomed: true });
  });

  it('formats icon string to single word', () => {
    const icons = [
      'clear-night',
      'rain',
      'partly-cloudy-day',
    ];
    const singleWords = icons.map(oneWordWeather);
    assert.deepEqual(singleWords, ['clear', 'rain', 'cloudy']);
  });

  it('renders daylight saving time from localMomentUnix', () => {
    const unix = 1465560000; // 10 June 2016 12:00
    const hour = localMomentUnix(unix).format('HH:mm');
    assert.equal(hour, '13:00'); // assert one hour ahead
  });

  it('renders message for stale data', () => {
    const {
      props: {
        children: message,
      },
    } = renderAtMoment(<WeatherTile {...props} />, new Date(2030, 1, 7));
    message.should.equal('Unable to show recent weather information');
  });

});
