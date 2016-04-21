// non-redux version
import UtilityBar from 'components/ui/UtilityBar';
import sinon from 'sinon';

describe('UtilityBar', () => {

  before(() => {
    // FIXME Rubbish that we have to set this, shouldn't rely on global state
    window.SSO = {LOGIN_URL: 'example.com/login', LOGOUT_URL: 'example.com/logout'};
  });

  after(() => {
    window.SSO = undefined;
  });

  it('renders nothing when empty', () => {
    let bar = <UtilityBar user={{empty:true}}/>;
    let result = shallowRender(bar);
    expect(result.type).to.equal('ul');
    expect(result.props.children).to.be.null;
  });

  it('renders Sign in link when we have no user', () => {
    let bar = <UtilityBar />;
    let result = shallowRender(bar);
    expect(result.type).to.eql('ul');
    const li = result.props.children;
    expect(li).to.have.property('type', 'li');
    expect(li.props.children).to.have.property('key', 'signInLink');
  });

  it('renders name when we are signed in on desktop', () => {
    const data = {name: 'Ron Swanson', authenticated: true};
    const bar = <UtilityBar user={{data}}/>;
    const result = shallowRender(bar);
    expect(result).to.have.property('type', 'ul');
    const link = result.props.children.props.children;
    expect(link).to.have.property('key', 'accountLink');
    expect(link).to.have.deep.property('props.data-toggle', 'id7:account-popover');
    expect(link).to.have.deep.property('props.children[0]', 'Ron Swanson');
  });

  it('renders image when we are signed in on mobile', () => {
    const props = {
      layoutClassName: 'mobile',
      user: {data: {name: 'Ron Swanson', authenticated: true}}
    };
    const bar = <UtilityBar {...props} />;
    const result = shallowRender(bar);
    expect(result).to.have.property('type', 'ul');
    const link = result.props.children.props.children;
    expect(link).to.have.property('type', 'a');
    const [ img ] = link.props.children;
    expect(img).to.have.deep.property('props.alt', 'Ron Swanson');
  })

});
