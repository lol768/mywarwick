// non-redux version
import UtilityBar from 'components/ui/UtilityBar';
import * as React from 'react';

describe('UtilityBar', () => {

  it('renders nothing when empty', () => {
    let bar = <UtilityBar user={{ empty: true, links: { login: '', logout: '' } }}/>;
    let result = shallowRender(bar);
    expect(result.type).to.equal('ul');
    expect(result.props.children).to.be.null;
  });

  it('renders Sign in link when we have no user', () => {
    let bar = <UtilityBar user={{ authenticated: false, links: { login: '', logout: '' } }} />;
    let result = shallowRender(bar);
    expect(result.type).to.eql('ul');
    const li = result.props.children;
    expect(li).to.have.property('type', 'li');
    expect(li.props.children).to.have.property('key', 'signInLink');
  });

  it('renders image when we are signed in', () => {
    const props = {
      user: {
        data: { name: 'Ron Swanson', authenticated: true, photo: { url: 'http://photos/photo/123' } },
        links: { login: '', logout: '' },
      },
    };
    const bar = <UtilityBar {...props} />;
    const result = shallowRender(bar);
    expect(result).to.have.property('type', 'ul');
    const link = result.props.children.props.children;
    expect(link).to.have.property('type', 'a');
    const [ img ] = link.props.children;
    expect(shallowRender(img)).to.have.deep.property('props.alt', 'Ron Swanson');
  })

});
