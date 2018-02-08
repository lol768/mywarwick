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


});