import TrafficCondition from 'components/tiles/traffic/TrafficCondition';

describe('TrafficCondition', () => {

  const usualTraffic = {
    route: { name: 'to A45' },
    "usualDuration": { "text": "5 mins", "value": 325 },
    "actualDuration": { "text": "5 mins", "value": 327 },
  };

  const mildTraffic = {
    route: { name: 'to A45' },
    "usualDuration": { "text": "5 mins", "value": 325 },
    "actualDuration": { "text": "10 mins", "value": 650 },
  };

  const heavyTraffic = {
    route: { name: 'to A45' },
    "usualDuration": { "text": "5 mins", "value": 325 },
    "actualDuration": { "text": "21 mins", "value": 1300 },
  };

  const usualCondition = shallowRender(<TrafficCondition {... usualTraffic} />);
  const mildCondition = shallowRender(<TrafficCondition {... mildTraffic} />);
  const heavyCondition = shallowRender(<TrafficCondition {... heavyTraffic} />);

  it('Shows the name of the route', () => {
    const [ , text] = usualCondition.props.children;
    text.props.children.should.equal(usualTraffic.route.name);
  });

  it('shows the correct icon depending on the delay', () => {
    let [i, ] = usualCondition.props.children;
    i.props.className.should.equal('fa fa-check-circle');
    [i, ] = mildCondition.props.children;
    i.props.className.should.equal('fa fa-hourglass-half');
    [i, ] = heavyCondition.props.children;
    i.props.className.should.equal('fa fa-exclamation-triangle');
  });

  it(`Shows the delay in minutes when traffic isn't usual`, () => {
    let [ , , delay] = usualCondition.props.children;
    expect(delay).to.equal('');
    [ , , delay] = mildCondition.props.children;
    expect(delay).to.equal(': +5 mins');
    [ , , delay] = heavyCondition.props.children;
    expect(delay).to.equal(': +16 mins');
  });

});
