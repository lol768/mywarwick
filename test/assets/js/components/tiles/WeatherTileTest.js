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
      ]
    }
  };

  it('displays a single weather item when not zoomed', () => {
    const html = shallowRender(<WeatherTile { ...props } />);
    html.type.should.equal('div');
    const [ weather ] = html.props.children;
    weather.type.should.equal('div');
    weather.props.className.should.equal('tile__item');
    const [temperature, skycon, text] = weather.props.children;
    temperature.type.should.equal('span');
    temperature.props.className.should.equal('tile__callout');
    temperature.props.children.should.equal('4°');
  });

  it('displays all items when zoomed', () => {
    const html = shallowRender(<WeatherTile zoomed={ true } { ...props } />);
    html.type.should.equal('div');
    html.props.children.length.should.equal(6);
  });

});
