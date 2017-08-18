import React from 'react';
import * as PropTypes from 'prop-types';
import { Checkbox, RadioButton } from '../../components/ui/Checkbox';
import { InputList, InputOptionsList } from './InputList';
import MultilineTextInput from './MultilineTextInput';
import _ from 'lodash-es';
import update from 'immutability-helper';
import modulePicker from '../../publish/modulePicker';
import seminarGroupPicker from '../../publish/seminarGroupPicker';
import relationshipPicker from '../../publish/relationshipPicker';

const GROUPS = {
  TeachingStaff: 'Teaching Staff',
  AdminStaff: 'Administrative Staff',
  TaughtPostgrads: 'Taught Postgraduates',
  ResearchPostgrads: 'Research Postgraduates',
};

const LOCATIONS = {
  CentralCampusResidences: 'Central campus residences',
  WestwoodResidences: 'Westwood residences',
  Coventry: 'Coventry',
  Kenilworth: 'Kenilworth',
  LeamingtonSpa: 'Leamington Spa',
};

export default class AudiencePicker extends React.PureComponent {
  static propTypes = {
    formData: PropTypes.object,
    formErrors: PropTypes.object,
    department: PropTypes.string,
  };

  constructor(props) {
    super(props);
    this.state = props.formData || {};

    this.handleChange = this.handleChange.bind(this);
    this.isChecked = this.isChecked.bind(this);
  }

  handleChange(value, type, path) {
    switch (type) {
      /* eslint-disable no-case-declarations */
      case 'checkbox':
        const existing = _.get(this.state, path, {});
        if (_.keys(existing).includes(value)) {
          const updated = _.pickBy(existing, (v, k) => k !== value);
          this.setState(state => _.set(_.cloneDeep(state), path, updated));
        } else {
          this.setState(state =>
            _.set(_.cloneDeep(state), path, _.assign({}, existing, { [value]: undefined })),
          );
        }
        /* eslint-enable no-case-declarations */
        break;
      case 'radio':
        this.setState(state => _.set(_.cloneDeep(state), path, { [value]: undefined }));
        break;
      case 'modules':
        this.setState(state =>
          update(state, { audience: { groups: { modules: { $set: value.items } } } }));
        break;
      case 'seminarGroups':
        this.setState(state =>
          update(state, { audience: { groups: { seminarGroups: { $set: value.items } } } }));
        break;
      case 'listOfUsercodes':
        this.setState(state =>
          update(state, { audience: { groups: { listOfUsercodes: { $set: value } } } }));
        break;
      case 'staffRelationships':
        this.setState(state =>
          update(state, { audience: { groups: { staffRelationships: { $set: value.items } } } }));
        break;
      default:
    }
  }

  isChecked(path) {
    return _.has(this.state, path);
  }

  render() {
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
              label={`Groups in ${this.props.department}`}
              name="groups"
              btnGroup="audience"
            >
              {Object.keys(GROUPS).map(key =>
                (<Checkbox
                  key={key}
                  handleChange={this.handleChange}
                  isChecked={this.isChecked(`audience.groups.${key}`)}
                  label={GROUPS[key]}
                  name={key}
                  btnGroup="audience.groups"
                />),
              )}

              <Checkbox
                handleChange={this.handleChange}
                isChecked={this.isChecked('audience.groups.UndergradStudents')}
                label="Undergraduates"
                name="UndergradStudents"
                btnGroup="audience.groups"
              >
                <RadioButton
                  handleChange={this.handleChange}
                  isChecked={this.isChecked('audience.groups.UndergradStudents.all')}
                  label="All"
                  name="all"
                  btnGroup="audience.groups.UndergradStudents"
                />
                <RadioButton
                  handleChange={this.handleChange}
                  isChecked={this.isChecked('audience.groups.UndergradStudents.year')}
                  label="Choose year of study"
                  name="year"
                  btnGroup="audience.groups.UndergradStudents"
                >
                  <Checkbox
                    handleChange={this.handleChange}
                    isChecked={this.isChecked('audience.groups.UndergradStudents.year.first')}
                    label="First year"
                    name="first"
                    btnGroup="audience.groups.UndergradStudents.year"
                  />
                  <Checkbox
                    handleChange={this.handleChange}
                    isChecked={this.isChecked('audience.groups.UndergradStudents.year.second')}
                    label="Second year"
                    name="second"
                    btnGroup="audience.groups.UndergradStudents.year"
                  />
                  <Checkbox
                    handleChange={this.handleChange}
                    isChecked={this.isChecked('audience.groups.UndergradStudents.year.final')}
                    label="Final year"
                    name="final"
                    btnGroup="audience.groups.UndergradStudents.year"
                  />
                </RadioButton>
              </Checkbox>
              <Checkbox
                handleChange={this.handleChange}
                isChecked={this.isChecked('audience.groups.modules')}
                label="Students taking a particular module"
                name="modules"
                btnGroup="audience.groups"
              >
                <InputList
                  name="modules"
                  handleChange={this.handleChange}
                  picker={modulePicker}
                  items={_.get(this.state, 'audience.groups.modules', [])}
                  placeholderText="Start typing to find a module"
                />
              </Checkbox>
              <Checkbox
                handleChange={this.handleChange}
                isChecked={this.isChecked('audience.groups.seminarGroups')}
                label="Students in a particular seminar group"
                name="seminarGroups"
                btnGroup="audience.groups"
              >
                <InputList
                  name="seminarGroups"
                  handleChange={this.handleChange}
                  picker={seminarGroupPicker}
                  items={_.get(this.state, 'audience.groups.seminarGroups', [])}
                  placeholderText="Start typing to find a seminar group"
                />
              </Checkbox>
              <Checkbox
                handleChange={this.handleChange}
                isChecked={this.isChecked('audience.groups.staffRelationships')}
                label="Students of a member of staff"
                name="staffRelationships"
                btnGroup="audience.groups"
              >
                <InputOptionsList
                  name="staffRelationships"
                  handleChange={this.handleChange}
                  picker={relationshipPicker}
                  items={_.get(this.state, 'audience.groups.staffRelationships', [])}
                  placeholderText="Start typing the name or usercode of the staff member"
                />
              </Checkbox>
              <Checkbox
                handleChange={this.handleChange}
                isChecked={this.isChecked('audience.groups.listOfUsercodes')}
                label="A list of people I'll type or paste in"
                name="listOfUsercodes"
                btnGroup="audience.groups"
              >
                <div>
                  <MultilineTextInput
                    name="listOfUsercodes"
                    handleChange={this.handleChange}
                    placeholder="Type in usercodes separated by commas (e.g. user1, user2)"
                  />
                </div>
              </Checkbox>
            </RadioButton>
          </div>

          <div className="list-group">
            <label>Is this alert specific to where people live?</label>
            <RadioButton
              handleChange={this.handleChange}
              isChecked={this.isChecked('locations.noLocation')}
              label="No"
              name="noLocation"
              btnGroup="locations"
            />
            <RadioButton
              handleChange={this.handleChange}
              isChecked={this.isChecked('locations.yesLocation')}
              label="Yes"
              name="yesLocation"
              btnGroup="locations"
            >
              {Object.keys(LOCATIONS).map(key =>
                (<Checkbox
                  key={key}
                  handleChange={(name, type, path) => this.handleChange(key, type, path)}
                  name="locations"
                  btnGroup="locations.yesLocation"
                  label={LOCATIONS[key]}
                  value={key}
                  isChecked={this.isChecked(`locations.yesLocation.${key}`)}
                />),
              )}
            </RadioButton>
          </div>

        </div>
      </div>
    );
  }
}
