import WeatherTile from 'components/tiles/WeatherTile';
import { renderToStaticMarkup } from 'react-dom/server';

describe('WeatherTile', () => {

  const props = {
    "content": {
      "items": [
        {"id": 1456316008, "time": 1456316008, "temp": 4.13, "icon": "clear-day", "text": "Clear"},
        {"id": 1456318800, "time": 1456318800, "temp": 4.71, "icon": "clear-day", "text": "Clear"},
        {"id": 1456322400, "time": 1456322400, "temp": 5.7, "icon": "clear-day", "text": "Clear"},
        {"id": 1456326000, "time": 1456326000, "temp": 6.71, "icon": "clear-day", "text": "Clear"},
        {"id": 1456329600, "time": 1456329600, "temp": 6.39, "icon": "clear-day", "text": "Clear"},
        {"id": 1456333200, "time": 1456333200, "temp": 5.47, "icon": "clear-day", "text": "Clear"}
      ],
      daily: {"summary": "all of today is going to suck!"},
    },
    size: 'small',
  };

  it('displays a single weather item when size small', () => {
    const html = shallowRender(<WeatherTile { ...props } />);
    html.type.should.equal('div');

    const [callout, caption, skycon] = html.props.children;
    callout.type.should.equal('span');
    callout.props.className.should.equal('tile__callout');
    callout.props.children[0].should.equal('4°');

    caption.type.should.equal('span');
    caption.props.className.should.equal('tile__text--caption');
    caption.props.children[9].should.equal('all of today is going to suck!');
  });

  it('displays large layout when zoomed', () => {
    const html = shallowRender(<WeatherTile zoomed={ true } { ...props } />);
    const [calloutContainer, captionContainer, items] = html.props.children;
    calloutContainer.props.children.props.className.should.equal('tile__callout');
    captionContainer.props.children.props.className.should.equal('tile__text--caption');
    items.length.should.equal(6);
  });

  it('formats icon string to single word', () => {
    const icons = [
      'clear-night',
      'rain',
      'partly-cloudy-day',
    ];
    const singleWords = icons.map(WeatherTile.oneWordWeather);
    assert.deepEqual(singleWords, ['clear', 'rain', 'cloudy']);
  })

});
