import { TileOptionView } from 'components/views/settings/TileOptionView';
import RadioListGroupItem from 'components/ui/RadioListGroupItem';
import SwitchListGroupItem from 'components/ui/SwitchListGroupItem';
import * as enzyme from 'enzyme';
import * as React from 'react';
import * as _ from 'lodash-es';

const props = {
  isOnline: true,
  dispatch: () => {},
  tile: {
    id: 'foo',
    title: 'Title',
    colour: 0,
    icon: 'cog',
  },
  tileOptions: {
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
};

const propsWithGroup = {
  isOnline: true,
  dispatch: () => {},
  tile: {
    id: 'foo',
    title: 'Title',
    colour: 0,
    icon: 'cog',
  },
  tileOptions: {
    radioOption: {
      default: 'value1',
      description: 'Description',
      groups: [
        {
          id: 'group1',
          name: 'group1name',
        },
        {
          id: 'group2',
          name: 'group2name'
        }
      ],
      options: [
        {
          name: 'Option 1',
          value: 'value1',
          group: 'group1',
        },
        {
          name: 'Option 2',
          value: 'value2',
          group: 'group1',
        },
        {
          name: 'Option 3',
          value: 'value3',
          group: 'group2',
        },
        {
          name: 'Option 4',
          value: 'value4',
          group: 'group2',
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
};

describe('Radio button', () => {

  const checkRadios = (radios) => {
    expect(radios.findWhere(r => r.prop('name') === 'radioOption')).to.have.length(2);
    expect(radios.findWhere(r => r.prop('value') === 'value1')).to.have.length(1);
    expect(radios.findWhere(r => r.prop('value') === 'value1')).to.have.length(1);
  };

  const checkRadioWithGroups = (radios) => {
    expect(radios.findWhere(r => r.prop('name') === 'radioOption')).to.have.length(4);
    expect(radios.findWhere(r => r.prop('value') === 'value1')).to.have.length(1);
    expect(radios.findWhere(r => r.prop('value') === 'value1')).to.have.length(1);
  };

  it('test', () => {
    const thisProps = _.cloneDeep(propsWithGroup);
    thisProps.tileOptions.radioOption.default = '';
    thisProps.tile.preferences = {};

    const result = enzyme.shallow(<TileOptionView {...thisProps}  />);
    const radios = result.find(RadioListGroupItem);
    checkRadioWithGroups(radios);
    radios.find('[checked="true"]').forEach((input) =>
      expect(input.props().checked).to.equal(false)
    )
  });

  it('unselected if no preference (undefined) and no default', () => {
    const thisProps = _.cloneDeep(props);
    thisProps.tileOptions.radioOption.default = '';
    thisProps.tile.preferences = {};

    const result = enzyme.shallow(<TileOptionView {...thisProps}  />);
    const radios = result.find(RadioListGroupItem);
    checkRadios(radios);
    radios.find('[checked="true"]').forEach((input) =>
      expect(input.props().checked).to.equal(false)
    )
  });

  it('unselected if no preference (blank) and no default', () => {
    const thisProps = _.cloneDeep(props);
    thisProps.tileOptions.radioOption.default = '';
    thisProps.tile.preferences = {
      radioOption: '',
    };

    const result = enzyme.shallow(<TileOptionView {...thisProps} />);
    const radios = result.find(RadioListGroupItem);
    checkRadios(radios);
    radios.find('[checked="true"]').forEach((input) =>
      expect(input.props().checked).to.equal(false)
    )
  });

  it('selected if no preference (undefined) with default', () => {
    const thisProps = _.cloneDeep(props);
    thisProps.tileOptions.radioOption.default = 'value1';
    thisProps.tile.preferences = {};

    const result = enzyme.shallow(<TileOptionView {...thisProps} />);
    const radios = result.find(RadioListGroupItem);
    checkRadios(radios);
    const checked = radios.findWhere((input) => input.props().checked);
    expect(checked).to.have.length(1);
    expect(checked.prop('value')).to.equal('value1');
  });

  it('selected if preference no default', () => {
    const thisProps = _.cloneDeep(props);
    thisProps.tile.preferences = {
      radioOption: 'value1',
    };

    const result = enzyme.shallow(<TileOptionView {...thisProps} />);
    const radios = result.find(RadioListGroupItem);
    checkRadios(radios);
    const checked = radios.findWhere((input) => input.props().checked);
    expect(checked).to.have.length(1);
    expect(checked.prop('value')).to.equal('value1');
  });

  it('selected if preference with default', () => {
    const thisProps = _.cloneDeep(props);
    thisProps.tileOptions.radioOption.default = 'value1';
    thisProps.tile.preferences = {
      radioOption: 'value2',
    };

    const result = enzyme.shallow(<TileOptionView {...thisProps} />);
    const radios = result.find(RadioListGroupItem);
    checkRadios(radios);
    const checked = radios.findWhere((input) => input.props().checked);
    expect(checked).to.have.length(1);
    expect(checked.prop('value')).to.equal('value2');
  });

});

describe('Checkboxes', () => {

  const checkCheckboxes = (checkboxes) => {
    expect(checkboxes.findWhere(r => r.prop('name') === 'checkboxOption')).to.have.length(2);
    expect(checkboxes.findWhere(r => r.prop('value') === 'value1')).to.have.length(1);
    expect(checkboxes.findWhere(r => r.prop('value') === 'value1')).to.have.length(1);
  };

  it('unselected if no preference (undefined) and no default', () => {
    const thisProps = _.cloneDeep(props);
    thisProps.tileOptions.checkboxOption.default = [];
    thisProps.tile.preferences = [];

    const result = enzyme.shallow(<TileOptionView {...thisProps} />);
    const checkboxes = result.find(SwitchListGroupItem);
    checkCheckboxes(checkboxes);
    checkboxes.find('[checked="true"]').forEach((input) =>
      expect(input.props().checked).to.equal(false)
    )
  });

  it('unselected if no preference (blank) and no default', () => {
    const thisProps = _.cloneDeep(props);
    thisProps.tileOptions.checkboxOption.default = [];
    thisProps.tile.preferences = {
      checkboxOption: [],
    };

    const result = enzyme.shallow(<TileOptionView {...thisProps} />);
    const checkboxes = result.find(SwitchListGroupItem);
    checkCheckboxes(checkboxes);
    checkboxes.find('[checked="true"]').forEach((input) =>
      expect(input.props().checked).to.equal(false)
    )
  });

  it('unselected if preference and no default', () => {
    const thisProps = _.cloneDeep(props);
    thisProps.tileOptions.checkboxOption.default = [];
    thisProps.tile.preferences = {
      checkboxOption: [
        {
          'value1': false,
        },
      ],
    };

    const result = enzyme.shallow(<TileOptionView {...thisProps} />);
    const checkboxes = result.find(SwitchListGroupItem);
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
    const checkboxes = result.find(SwitchListGroupItem);
    checkCheckboxes(checkboxes);
    checkboxes.find('[checked="true"]').forEach((input) =>
      expect(input.props().checked).to.equal(false)
    )
  });

  it('selected if preference and no default', () => {
    const thisProps = _.cloneDeep(props);
    thisProps.tileOptions.checkboxOption.default = [];
    thisProps.tile.preferences = {
      checkboxOption: {
        'value1': true,
      },
    };

    const result = enzyme.shallow(<TileOptionView {...thisProps} />);
    const checkboxes = result.find(SwitchListGroupItem);
    checkCheckboxes(checkboxes);
    const checked = checkboxes.findWhere((input) => input.props().checked);
    expect(checked).to.have.length(1);
    expect(checked.prop('value')).to.equal('value1');
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
    const checkboxes = result.find(SwitchListGroupItem);
    checkCheckboxes(checkboxes);
    const checked = checkboxes.findWhere((input) => input.props().checked);
    expect(checked).to.have.length(1);
    expect(checked.prop('value')).to.equal('value1');
  });

  it('selected if no preference (undefined) with default', () => {
    const thisProps = _.cloneDeep(props);
    thisProps.tile.preferences = {};

    const result = enzyme.shallow(<TileOptionView {...thisProps} />);
    const checkboxes = result.find(SwitchListGroupItem);
    checkCheckboxes(checkboxes);
    const checked = checkboxes.findWhere((input) => input.props().checked);
    expect(checked).to.have.length(2);
  });

});