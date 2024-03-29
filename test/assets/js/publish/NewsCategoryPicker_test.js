import { NewsCategoryPicker } from 'publish/components/NewsCategoryPicker';
import { RadioButton, Checkbox } from '../../../../app/assets/js/components/ui/Checkbox';
import * as React from 'react';
import * as enzyme from 'enzyme';
import _ from 'lodash-es';

const context = {
  store: {
    dispatch: () => {
    },
  },
};

const props = {
  newsCategories: [
    {
      id: 'mac',
      name: 'news about mac',
    },
    {
      id: 'pc',
      name: 'news about pc',
    },
    {
      id: 'linux',
      name: 'news about linux'
    }
  ],
  formData: {},
  audienceDidUpdate: () => {
  },
};

describe('NewsCategoryPicker', () => {

  it('should set initial state properly if formData is not supplied', () => {
    let shallow = enzyme.shallow(<NewsCategoryPicker {...props} />);
    expect(shallow.state().ignoreCategories).to.eql(false);
    expect(shallow.state().chosenCategories).to.eql([]);

    shallow = enzyme.shallow(<NewsCategoryPicker {...{ ...props, formData: null }} />);
    expect(shallow.state().ignoreCategories).to.eql(false);
    expect(shallow.state().chosenCategories).to.eql([]);

  });

  it('should set initial state according to supplied formData', () => {
    let shallow = enzyme.shallow(<NewsCategoryPicker {...{
      ...props, formData: {
        ignoreCategories: true,
        chosenCategories: ['mac', 'linux']
      }
    }} />);
    expect(shallow.state().ignoreCategories).to.eql(true);
    expect(shallow.state().chosenCategories).to.eql(['mac', 'linux']);
  });

  it('should should render the correct number of checkboxes', () => {
    let shallow = enzyme.shallow(<NewsCategoryPicker {...props} />);
    expect(shallow.find(Checkbox).length).to.eql(4);
  });

  it('should update states according to checkbox changes', () => {
    let wrapper = enzyme.mount(<NewsCategoryPicker {...props} />, { context });

    wrapper.find(Checkbox).first().find('input').simulate('change');
    expect(wrapper.state().chosenCategories.length).to.eql(1);

    wrapper.find(Checkbox).first().find('input').simulate('change');
    expect(wrapper.state().chosenCategories.length).to.eql(0);

    wrapper.find(Checkbox).first().find('input').simulate('change');
    wrapper.find(Checkbox).at(1).find('input').simulate('change');
    wrapper.find(Checkbox).last().find('input').simulate('change');
    expect(wrapper.state().chosenCategories.length).to.eql(2);
    expect(wrapper.state().ignoreCategories).to.eql(true);

  })


});