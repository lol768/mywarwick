import { NewsCategoryPicker } from 'publish/components/NewsCategoryPicker';
import { Checkbox } from '../../../../app/assets/js/components/ui/Checkbox';
import * as React from 'react';
import * as enzyme from 'enzyme';
import _ from 'lodash-es';

const context = {
  store: {
    dispatch: () => {},
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
  updateAudience: () => {},
};

describe('NewsCategoryPicker', () => {
  it('should set initial state properly if formData is not supplied', () => {
    let shallow = enzyme.shallow(<NewsCategoryPicker {...props} />);
    shallow.state().ignoreCategories.should.eql(false);
    shallow.state().chosenCategories.should.eql([]);

    shallow = enzyme.shallow(<NewsCategoryPicker {...{ ...props, formData: null }} />);
    shallow.state().ignoreCategories.should.eql(false);
    shallow.state().chosenCategories.should.eql([]);

  });

  it('should set initial state according to supplied formData', () => {
    let shallow = enzyme.shallow(<NewsCategoryPicker {...{
      ...props, formData: {
        ignoreCategories: true,
        chosenCategories: ['mac', 'linux']
      }
    }} />);
    shallow.state().ignoreCategories.should.eql(true);
    shallow.state().chosenCategories.should.eql(['mac', 'linux']);
  });

  it('should should render the correct number of checkboxes', () => {
    let shallow = enzyme.shallow(<NewsCategoryPicker {...props} />);
    shallow.find(Checkbox).length.should.eql(4);
  });

  it('should update states according to checkbox changes', () => {
    let wrapper = enzyme.mount(<NewsCategoryPicker {...props} />, { context });

    wrapper.find(Checkbox).first().find('input').simulate('change');
    wrapper.state().chosenCategories.length.should.eql(1);

    wrapper.find(Checkbox).first().find('input').simulate('change');
    wrapper.state().chosenCategories.length.should.eql(0);

    wrapper.find(Checkbox).first().find('input').simulate('change');
    wrapper.find(Checkbox).at(1).find('input').simulate('change');
    wrapper.find(Checkbox).last().find('input').simulate('change');
    wrapper.state().chosenCategories.length.should.eql(2);
    wrapper.state().ignoreCategories.should.eql(true);
  })
});
