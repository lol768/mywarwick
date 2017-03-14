import TrafficTile from 'components/tiles/traffic/TrafficTile';
import tk from 'timekeeper';

const STALE_AFTER = 1200000;

describe('TrafficTile', () => {

  let props;

  function renderAtMs(now) {
    tk.freeze(new Date(now));
    props.fetchedAt = now;
    return ReactTestUtils.renderIntoDocument(<TrafficTile {... props} />);
  }

  // Re-initialise the spy before each test
  beforeEach(() => {
    props = {
      size: 'wide',
      content: {
        items: [
          {
            "route": {
              "name": "from A45",
              "inbound": true,
              "start": { "latitude": 52.390173, "longitude": -1.555059 },
              "end": { "latitude": 52.379436, "longitude": -1.560668 }
            },
            "summary": "Charter Ave",
            "usualDuration": { "text": "5 mins", "value": 291 },
            "actualDuration": { "text": "5 mins", "value": 315 }
          },
          {
            "route": {
              "name": "to A45",
              "inbound": false,
              "start": { "latitude": 52.379436, "longitude": -1.560668 },
              "end": { "latitude": 52.390173, "longitude": -1.555059 },
            },
            "summary": "Charter Ave",
            "usualDuration": { "text": "5 mins", "value": 291 },
            "actualDuration": { "text": "5 mins", "value": 315 }
          },
        ],
        alerts: {
          items: [
            {
              title: 'Herons are blocking Gibbet Hill road',
              url: { href: 'http://www2.warwick.ac.uk/services/campus-support/alerts/herons', },
            }
          ]
        },
      },
    };
  });

  it('Shows an error message rather than stale data', () => {
    tk.freeze(new Date(1440289200001));
    props.fetchedAt = 1440288000000;
    const html = shallowRender(<TrafficTile {... props} />);
    html.type.should.equal('div');
    html.props.children.should.equal('Unable to show recent traffic information.');
  });

  it('Shows inboud Traffic conditions before home time', () => {
    const html = renderAtMs(1440288000000);
    const routeNameContainer = ReactTestUtils.findRenderedDOMComponentWithClass(html, 'route-name');
    expect(routeNameContainer.textContent).to.equal('from A45');
  });

  it('Shows outbound Traffic conditions after home time', () => {
    const html = renderAtMs(1440347400000);
    const routeNameContainer = ReactTestUtils.findRenderedDOMComponentWithClass(html, 'route-name');
    expect(routeNameContainer.textContent).to.equal('to A45');
  });

  it('Shows the alert count when not zoomed', () => {
    const html = renderAtMs(1440347400000);
    const alertCount = ReactTestUtils.findRenderedDOMComponentWithClass(html, 'alert-count');
    expect(alertCount.textContent).to.equal('1 traffic alert');
  });

  it('Shows the full alert when zoomed. No more link is shown.', () => {
    props.zoomed = true;
    const html = renderAtMs(1440347400000);
    const alertCount = ReactTestUtils.scryRenderedDOMComponentsWithClass(html, 'traffic-alert');
    expect(alertCount.length).to.equal(1);
    const moreLink = ReactTestUtils.scryRenderedDOMComponentsWithClass(html, 'more-alerts');
    expect(moreLink.length).to.equal(0);
  });

  it('Only shows the first two alerts when zoomed, followed by a more link.', () => {
    props.zoomed = true;
    props.content.alerts.items.push({
      title: 'Health center road is on fire',
      url: { href: 'http://www2.warwick.ac.uk/services/campus-support/alerts/fire', },
    });
    props.content.alerts.items.push({
      title: 'Third alert that is not shown',
      url: { href: 'http://www2.warwick.ac.uk/services/campus-support/alerts/third', },
    });
    const html = renderAtMs(1440347400000);
    const alertCount = ReactTestUtils.scryRenderedDOMComponentsWithClass(html, 'traffic-alert');
    expect(alertCount.length).to.equal(2);
    const moreLink = ReactTestUtils.scryRenderedDOMComponentsWithClass(html, 'more-alerts');
    expect(moreLink.length).to.equal(1);
  });

});