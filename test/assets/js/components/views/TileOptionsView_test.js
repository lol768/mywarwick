import TileOptionView from 'components/views/TileOptionView';
import * as enzyme from 'enzyme';
import * as React from 'react';
import * as _ from 'lodash-es';

const props = {
  tile: {
    id: 'foo',
    title: 'Title',
    option: {
      radioOption: {
        default: 'value1',
        description: 'Description',
        options: [
          {
            name: 'Option 1',
            value: 'value1',
          },
          {
            name: 'Option 2',
            value: 'value2',
          },
        ],
        type: 'string',
      },
      checkboxOption: {
        default: ['value1', 'value2'],
        description: 'Description',
        options: [
          {
            name: 'Option 1',
            value: 'value1',
          },
          {
            name: 'Option 2',
            value: 'value2',
          },
        ],
        type: 'array',
      }
    },
  },
  onConfigViewDismiss: () => {},
  onConfigSave: () => {},
};

describe('Radio button', () => {

  const checkRadios = (radios) => {
    expect(radios.find('[name="radioOption"]')).to.have.length(2);
    expect(radios.find('[value="value1"]')).to.have.length(1);
    expect(radios.find('[value="value2"]')).to.have.length(1);
  };

  it('unselected if no preference (undefined) and no default', () => {
    const thisProps = _.cloneDeep(props);
    thisProps.tile.option.radioOption.default = '';
    thisProps.tile.preferences = {};

    const result = enzyme.shallow(<TileOptionView {...thisProps} />);
    const radios = result.find('input[type="radio"]');
    checkRadios(radios);
    radios.find('[checked="true"]').forEach((input) =>
      expect(input.props().checked).to.equal(false)
    )
  });

  it('unselected if no preference (blank) and no default', () => {
    const thisProps = _.cloneDeep(props);
    thisProps.tile.option.radioOption.default = '';
    thisProps.tile.preferences = {
      radioOption: '',
    };

    const result = enzyme.shallow(<TileOptionView {...thisProps} />);
    const radios = result.find('input[type="radio"]');
    checkRadios(radios);
    radios.find('[checked="true"]').forEach((input) =>
      expect(input.props().checked).to.equal(false)
    )
  });

  it('selected if no preference (undefined) with default', () => {
    const thisProps = _.cloneDeep(props);
    thisProps.tile.option.radioOption.default = 'value1';
    thisProps.tile.preferences = {};

    const result = enzyme.shallow(<TileOptionView {...thisProps} />);
    const radios = result.find('input[type="radio"]');
    checkRadios(radios);
    const checked = radios.findWhere((input) => input.props().checked);
    expect(checked).to.have.length(1);
    expect(checked.props().value).to.equal('value1');
  });

  it('selected if no preference (blank) with default', () => {
    const thisProps = _.cloneDeep(props);
    thisProps.tile.option.radioOption.default = 'value1';
    thisProps.tile.preferences = {
      radioOption: '',
    };

    const result = enzyme.shallow(<TileOptionView {...thisProps} />);
    const radios = result.find('input[type="radio"]');
    checkRadios(radios);
    const checked = radios.findWhere((input) => input.props().checked);
    expect(checked).to.have.length(1);
    expect(checked.props().value).to.equal('value1');
  });

  it('selected if preference no default', () => {
    const thisProps = _.cloneDeep(props);
    thisProps.tile.preferences = {
      radioOption: 'value1',
    };

    const result = enzyme.shallow(<TileOptionView {...thisProps} />);
    const radios = result.find('input[type="radio"]');
    checkRadios(radios);
    const checked = radios.findWhere((input) => input.props().checked);
    expect(checked).to.have.length(1);
    expect(checked.props().value).to.equal('value1');
  });

  it('selected if preference with default', () => {
    const thisProps = _.cloneDeep(props);
    thisProps.tile.option.radioOption.default = 'value1';
    thisProps.tile.preferences = {
      radioOption: 'value2',
    };

    const result = enzyme.shallow(<TileOptionView {...thisProps} />);
    const radios = result.find('input[type="radio"]');
    checkRadios(radios);
    const checked = radios.findWhere((input) => input.props().checked);
    expect(checked).to.have.length(1);
    expect(checked.props().value).to.equal('value2');
  });

});

describe('Checkboxes', () => {

  const checkCheckboxes = (checkboxes) => {
    expect(checkboxes.find('[name="checkboxOption"]')).to.have.length(2);
    expect(checkboxes.find('[value="value1"]')).to.have.length(1);
    expect(checkboxes.find('[value="value2"]')).to.have.length(1);
  };

  it('unselected if no preference (undefined) and no default', () => {
    const thisProps = _.cloneDeep(props);
    thisProps.tile.option.checkboxOption.default = [];
    thisProps.tile.preferences = [];

    const result = enzyme.shallow(<TileOptionView {...thisProps} />);
    const checkboxes = result.find('input[type="checkbox"]');
    checkCheckboxes(checkboxes);
    checkboxes.find('[checked="true"]').forEach((input) =>
      expect(input.props().checked).to.equal(false)
    )
  });

  it('unselected if no preference (blank) and no default', () => {
    const thisProps = _.cloneDeep(props);
    thisProps.tile.option.checkboxOption.default = [];
    thisProps.tile.preferences = {
      checkboxOption: [],
    };

    const result = enzyme.shallow(<TileOptionView {...thisProps} />);
    const checkboxes = result.find('input[type="checkbox"]');
    checkCheckboxes(checkboxes);
    checkboxes.find('[checked="true"]').forEach((input) =>
      expect(input.props().checked).to.equal(false)
    )
  });

  it('unselected if preference and no default', () => {
    const thisProps = _.cloneDeep(props);
    thisProps.tile.option.checkboxOption.default = [];
    thisProps.tile.preferences = {
      checkboxOption: [
        {
          'value1': false,
        },
      ],
    };

    const result = enzyme.shallow(<TileOptionView {...thisProps} />);
    const checkboxes = result.find('input[type="checkbox"]');
    checkCheckboxes(checkboxes);
    checkboxes.find('[checked="true"]').forEach((input) =>
      expect(input.props().checked).to.equal(false)
    )
  });

  it('unselected if preference with default', () => {
    const thisProps = _.cloneDeep(props);
    thisProps.tile.preferences = {
      checkboxOption: {
        'value1': false,
        'value2': false,
      },
    };

    const result = enzyme.shallow(<TileOptionView {...thisProps} />);
    const checkboxes = result.find('input[type="checkbox"]');
    checkCheckboxes(checkboxes);
    checkboxes.find('[checked="true"]').forEach((input) =>
      expect(input.props().checked).to.equal(false)
    )
  });

  it('selected if preference and no default', () => {
    const thisProps = _.cloneDeep(props);
    thisProps.tile.option.checkboxOption.default = [];
    thisProps.tile.preferences = {
      checkboxOption: {
        'value1': true,
      },
    };

    const result = enzyme.shallow(<TileOptionView {...thisProps} />);
    const checkboxes = result.find('input[type="checkbox"]');
    checkCheckboxes(checkboxes);
    const checked = checkboxes.findWhere((input) => input.props().checked);
    expect(checked).to.have.length(1);
    expect(checked.props().value).to.equal('value1');
  });

  it('selected if preference with default', () => {
    const thisProps = _.cloneDeep(props);
    thisProps.tile.preferences = {
      checkboxOption: {
        'value1': true,
        'value2': false,
      },
    };

    const result = enzyme.shallow(<TileOptionView {...thisProps} />);
    const checkboxes = result.find('input[type="checkbox"]');
    checkCheckboxes(checkboxes);
    const checked = checkboxes.findWhere((input) => input.props().checked);
    expect(checked).to.have.length(1);
    expect(checked.props().value).to.equal('value1');
  });

  it('selected if no preference (undefined) with default', () => {
    const thisProps = _.cloneDeep(props);
    thisProps.tile.preferences = {};

    const result = enzyme.shallow(<TileOptionView {...thisProps} />);
    const checkboxes = result.find('input[type="checkbox"]');
    checkCheckboxes(checkboxes);
    const checked = checkboxes.findWhere((input) => input.props().checked);
    expect(checked).to.have.length(2);
  });

  it('selected if preference (legacy) and no default', () => {
    const thisProps = _.cloneDeep(props);
    thisProps.tile.option.checkboxOption.default = [];
    thisProps.tile.preferences = {
      checkboxOption: [
        'value1',
      ],
    };

    const result = enzyme.shallow(<TileOptionView {...thisProps} />);
    const checkboxes = result.find('input[type="checkbox"]');
    checkCheckboxes(checkboxes);
    const checked = checkboxes.findWhere((input) => input.props().checked);
    expect(checked).to.have.length(1);
    expect(checked.props().value).to.equal('value1');
  });

  it('selected if preference (legacy) with default', () => {
    const thisProps = _.cloneDeep(props);
    thisProps.tile.preferences = {
      checkboxOption: [
        'value1',
      ],
    };

    const result = enzyme.shallow(<TileOptionView {...thisProps} />);
    const checkboxes = result.find('input[type="checkbox"]');
    checkCheckboxes(checkboxes);
    const checked = checkboxes.findWhere((input) => input.props().checked);
    // Even though value2 defaults to true
    // we can't use the defaults with the legacy (array) structure
    expect(checked).to.have.length(1);
    expect(checked.props().value).to.equal('value1');
  });

});