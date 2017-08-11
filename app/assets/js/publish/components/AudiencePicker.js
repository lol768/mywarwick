import React from 'react';
import * as PropTypes from 'prop-types';
import { Checkbox, RadioButton } from '../../components/ui/Checkbox';
import InputList from '../../components/ui/InputList';
import _ from 'lodash-es';

export default class AudiencePicker extends React.PureComponent {

  static propTypes = {
    selected: PropTypes.object
  };

  constructor(props) {
    super(props);
    this.state = props.selected || {};

    this.handleChange = this.handleChange.bind(this);
    this.isChecked = this.isChecked.bind(this);
  }

  handleChange({ target: { name } }, path, type) {
    if (type === 'checkbox') {
      const existing = _.get(this.state, path, {});
      if (_.keys(existing).includes(name)) {
        const updated = _.pickBy(existing, (v, k) => k !== name);
        this.setState(state => _.set(_.cloneDeep(state), path, updated));
      } else {
        this.setState(state =>
          _.set(_.cloneDeep(state), path, _.assign({}, existing, { [name]: undefined }))
        );
      }
    } else {
      this.setState(state => _.set(_.cloneDeep(state), path, { [name]: undefined }));
    }
  }

  isChecked(path) {
    return _.has(this.state, path);
  }

  render() {
    const options = {
      'teaching': 'Teaching Staff',
      'admin': 'Administrative Staff',
      'taught': 'Taught Postgraduates',
      'research': 'Research Postgraduates'
    };

    return (
      <div>
        <div className="form-group">

          <div className="list-group">
            <label>Who is this alert for?</label>
            <RadioButton
              handleChange={this.handleChange}
              isChecked={this.isChecked('audience.everyone')}
              label="Everyone"
              name="everyone"
              btnGroup="audience"
            />
            <RadioButton
              handleChange={this.handleChange}
              isChecked={this.isChecked('audience.groups')}
              label="Groups in <dept>"
              name="groups"
              btnGroup="audience"
            >
              {Object.keys(options).map(value =>
                <Checkbox
                  key={value}
                  handleChange={this.handleChange}
                  isChecked={this.isChecked(`audience.groups.${value}`)}
                  label={options[value]}
                  name={value}
                  btnGroup="audience.groups"
                />
              )}

              <Checkbox
                handleChange={this.handleChange}
                isChecked={this.isChecked('audience.groups.ug')}
                label='Undergraduates'
                name='ug'
                btnGroup="audience.groups"
              >
                <RadioButton
                  handleChange={this.handleChange}
                  isChecked={this.isChecked('audience.groups.ug.all')}
                  label="All"
                  name="all"
                  btnGroup="audience.groups.ug"
                />
                <RadioButton
                  handleChange={this.handleChange}
                  isChecked={this.isChecked('audience.groups.ug.year')}
                  label="Choose year of study"
                  name="year"
                  btnGroup="audience.groups.ug"
                >
                  <Checkbox
                    handleChange={this.handleChange}
                    isChecked={this.isChecked('audience.groups.ug.year.first')}
                    label='First year'
                    name='first'
                    btnGroup="audience.groups.ug.year"
                  />
                  <Checkbox
                    handleChange={this.handleChange}
                    isChecked={this.isChecked('audience.groups.ug.year.second')}
                    label='Second year'
                    name='second'
                    btnGroup="audience.groups.ug.year"
                  />
                  <Checkbox
                    handleChange={this.handleChange}
                    isChecked={this.isChecked('audience.groups.ug.year.final')}
                    label='Final year'
                    name='final'
                    btnGroup="audience.groups.ug.year"
                  />
                </RadioButton>
              </Checkbox>
              <Checkbox
                handleChange={this.handleChange}
                isChecked={this.isChecked('audience.groups.module')}
                label='Students taking a particular module'
                name='module'
                btnGroup="audience.groups"
              >
                <InputList name="modules" items={[]} />
              </Checkbox>
              <Checkbox
                handleChange={this.handleChange}
                isChecked={this.isChecked('audience.groups.seminar')}
                label='Students in a particular seminar group'
                name='seminar'
                btnGroup="audience.groups"
              >
              </Checkbox>
              <Checkbox
                handleChange={this.handleChange}
                isChecked={this.isChecked('audience.groups.studentsOfStaff')}
                label='Students of a particular member of staff'
                name='studentsOfStaff'
                btnGroup="audience.groups"
              >
              </Checkbox>
            </RadioButton>
          </div>
        </div>
      </div>
    )
  }
}
