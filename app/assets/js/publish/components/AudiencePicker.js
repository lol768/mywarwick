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

  handleChange({ target: { value } }, path, type) {
    if (type === 'checkbox') {
      const existing = _.get(this.state, path, {});
      if (_.keys(existing).includes(value)) {
        const updated = _.pickBy(existing, (v, k) => k !== value);
        this.setState(state => _.set(_.cloneDeep(state), path, updated));
      } else {
        this.setState(state =>
          _.set(_.cloneDeep(state), path, _.assign({}, existing, { [value]: undefined }))
        );
      }
    } else {
      this.setState(state => _.set(_.cloneDeep(state), path, { [value]: undefined }));
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
              isChecked={this.isChecked('top.everyone')}
              label="Everyone"
              value="everyone"
              btnGroup="top"
            />
            <RadioButton
              handleChange={this.handleChange}
              isChecked={this.isChecked('top.groups')}
              label="Groups in <dept>"
              value="groups"
              btnGroup="top"
            >
              {Object.keys(options).map(value =>
                <Checkbox
                  key={value}
                  handleChange={this.handleChange}
                  isChecked={this.isChecked(`top.groups.${value}`)}
                  label={options[value]}
                  value={value}
                  btnGroup="top.groups"
                />
              )}

              <Checkbox
                handleChange={this.handleChange}
                isChecked={this.isChecked('top.groups.ug')}
                label='Undergraduates'
                value='ug'
                btnGroup="top.groups"
              >
                <RadioButton
                  handleChange={this.handleChange}
                  isChecked={this.isChecked('top.groups.ug.all')}
                  label="All"
                  value="all"
                  btnGroup="top.groups.ug"
                />
                <RadioButton
                  handleChange={this.handleChange}
                  isChecked={this.isChecked('top.groups.ug.year')}
                  label="Choose year of study"
                  value="year"
                  btnGroup="top.groups.ug"
                >
                  <Checkbox
                    handleChange={this.handleChange}
                    isChecked={this.isChecked('top.groups.ug.year.first')}
                    label='First year'
                    value='first'
                    btnGroup="top.groups.ug.year"
                  />
                  <Checkbox
                    handleChange={this.handleChange}
                    isChecked={this.isChecked('top.groups.ug.year.second')}
                    label='Second year'
                    value='second'
                    btnGroup="top.groups.ug.year"
                  />
                  <Checkbox
                    handleChange={this.handleChange}
                    isChecked={this.isChecked('top.groups.ug.year.final')}
                    label='Final year'
                    value='final'
                    btnGroup="top.groups.ug.year"
                  />
                </RadioButton>
              </Checkbox>
              <Checkbox
                handleChange={this.handleChange}
                isChecked={this.isChecked('top.groups.module')}
                label='Students taking a particular module'
                value='module'
                btnGroup="top.groups"
              >
                <InputList/>
              </Checkbox>
              <Checkbox
                handleChange={this.handleChange}
                isChecked={this.isChecked('top.groups.seminar')}
                label='Students in a particular seminar group'
                value='seminar'
                btnGroup="top.groups"
              />
              <Checkbox
                handleChange={this.handleChange}
                isChecked={this.isChecked('top.groups.studentsOfStaff')}
                label='Students of a particular member of staff'
                value='studentsOfStaff'
                btnGroup="top.groups"
              />
            </RadioButton>
          </div>
        </div>
      </div>
    )
  }
}
