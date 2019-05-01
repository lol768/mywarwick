import TrafficAlert from 'components/tiles/traffic/TrafficAlert';

describe('TrafficAlert', () => {

  it('should render an alert correctly', () => {
    const props = {
      title: 'Herons are blocking Gibbet Hill road',
      href: 'http://www2.warwick.ac.uk/services/campus-support/alerts/herons',
    };

    const html = shallowRender(<TrafficAlert {...props} />);

    html.should.deep.equal(
      <div className="traffic-alert">
        <i className="fal fa-fw fa-exclamation-triangle" />
        <a href={props.href}>{props.title}</a>
      </div>
    );
  });
});
