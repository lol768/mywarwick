import ColourSchemesView from 'components/views/settings/ColourSchemesView';
import { shallow } from 'enzyme';
import * as React from 'react';
import { expect } from 'chai';

const propsTestData = {
  fetching: false,
  failed: false,
  fetched: true,
  chosen: 1,
  schemes: [
    {
      "id": 1,
      "url": "warwick_goose_reservation.jpg",
      "name": "Geese"
    },
    {
      "id": 2,
      "url": "koan.jpg",
      "name": "Koan (Gibbet Hill)"
    }
  ],
  isOnline: true,
};

describe('ColourSchemesViews', () => {
  it('includes all of the colour schemes passed to it', () => {
    const result = shallow(<ColourSchemesView.WrappedComponent {...propsTestData} />);
    expect(result.find('.media-body-colour-scheme-choice')).to.have.length(2);
    expect(result.find('.media-body-colour-scheme-choice').first().text()).to.contain("Geese");
    expect(result.find('.media-body-colour-scheme-choice').last().text()).to.contain("Koan (Gibbet Hill)");
  });

  it('selects the selected scheme only', () => {
    const result = shallow(<ColourSchemesView.WrappedComponent {...propsTestData} />);
    const radios = result.find('[type="radio"]');
    expect(radios.last().props().checked).to.equal(false);
    expect(radios.first().props().checked).to.equal(true);
  });

  it('selects the correct scheme after it is clicked', () => {
    const result = shallow(<ColourSchemesView.WrappedComponent {...propsTestData} />);
    result.find('.list-group-item-colour-scheme').last().simulate('click', {
      type: 'click',
      currentTarget: { blur: () => {} }
    });

    const radios = result.find('[type="radio"]');

    expect(radios.last().props().checked).to.equal(true);
    expect(radios.first().props().checked).to.equal(false);
  });

  it('selects the correct scheme after enter pressed', () => {
    const result = shallow(<ColourSchemesView.WrappedComponent {...propsTestData} />);
    result.find('.list-group-item-colour-scheme').last().simulate('click', {
      type: 'keyup',
      keyCode: 13,
      preventDefault: () => {}
    });
    const radios = result.find('[type="radio"]');

    expect(radios.last().props().checked).to.equal(true);
    expect(radios.first().props().checked).to.equal(false);
  });
});
