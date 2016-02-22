import TrafficAlert from 'components/tiles/traffic/TrafficAlert';

describe('TrafficAlert', () => {

  it('should render an alert correctly', () => {
    const props = {
      title: 'Herons are blocking Gibbet Hill road',
      href: 'http://www2.warwick.ac.uk/services/campus-support/alerts/herons',
    };

    const jsx = (
      <TrafficAlert {...props} />
    );

    const html = shallowRender(jsx);
    html.type.should.equal('div');

    const [ i, a ] = html.props.children;
    i.type.should.equal('i');
    i.props.className.should.equal('fa fa-exclamation-triangle');
    a.type.should.equal('a');
    a.props.href.should.equal(props.href);
    a.props.children.should.equal(props.title);
  });
});
