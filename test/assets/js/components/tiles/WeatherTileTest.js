import WeatherTile from 'components/tiles/WeatherTile';
import { renderToStaticMarkup } from 'react-dom/server';
import { localMomentUnix } from 'dateFormatter';

describe('WeatherTile', () => {

  const data = {
      icon: 'clear-day',
      temp: 4.13,
      text: 'Clear',
      precipProbability: 0.55
    },
    props = {
      "content": {
        "items": [
          {"id": 1456316008, "time": 1456316008, ...data},
          {"id": 1456318800, "time": 1456318800, ...data},
          {"id": 1456322400, "time": 1456322400, ...data},
          {"id": 1456326000, "time": 1456326000, ...data},
          {"id": 1456329600, "time": 1456329600, ...data},
          {"id": 1456333200, "time": 1456333200, ...data},
        ],
        daily: {"summary": "all of today is going to suck!"},
      },
      size: 'small',
    };

  it('displays a single weather item when size small', () => {
    const html = shallowRender(<WeatherTile { ...props } />);
    html.type.should.equal('div');

    const callout = shallowRender(html.props.children[0]);
    callout.type.should.equal('span');
    callout.props.className.should.equal('tile__callout');
    callout.props.children[0].should.equal(4); // the ° falls into the next child component

    const caption = shallowRender(html.props.children[1]);
    caption.type.should.equal('span');
    caption.props.className.should.equal('tile__text--caption');
    caption.props.children[1]
      .props.children[5].should.equal('all of today is going to suck!');
  });

  it('displays large layout when zoomed', () => {
    const html = shallowRender(<WeatherTile zoomed={ true } { ...props } />);
    const [calloutContainer, captionContainer, weatherTable] = html.props.children;
    const callout = shallowRender(calloutContainer.props.children);
    callout.props.className.should.equal('tile__callout');
    const caption = shallowRender(captionContainer.props.children);
    caption.props.className.should.equal('tile__text--caption');
    const table = shallowRender(weatherTable);
    table.props.children.length.should.equal(6);
    table.props.children[0]
      .props.children[2]
      .props.children[2].should.equal(55); // precipProbability is rendered
  });

  it('formats icon string to single word', () => {
    const icons = [
      'clear-night',
      'rain',
      'partly-cloudy-day',
    ];
    const singleWords = icons.map(WeatherTile.oneWordWeather);
    assert.deepEqual(singleWords, ['clear', 'rain', 'cloudy']);
  });

  it('renders daylight saving time from localMomentUnix', () => {
    const unix = 1465560000; // 10 June 2016 12:00
    const hour = localMomentUnix(unix).format('HH:mm');
    assert.equal(hour, '13:00'); // assert one hour ahead
  });

});
