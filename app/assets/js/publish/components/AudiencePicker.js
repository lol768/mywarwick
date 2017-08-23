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
    isGod: PropTypes.bool,
    departments: PropTypes.arrayOf(PropTypes.arrayOf(PropTypes.string)),
  };

  constructor(props) {
    super(props);
    const { departments: depts } = props;
    const state = {
      department: depts.length > 1 ? '...' : depts[0][1],
    };
    this.state = props.formData || state;

    this.handleChange = this.handleChange.bind(this);
    this.isChecked = this.isChecked.bind(this);
    this.selectDepartment = this.selectDepartment.bind(this);
  }

  handleChange(value, type, path) {
    const updateObj = obj => (
      { audience: { [this.isChecked('audience.university') ? 'university' : 'department']: { groups: obj } } }
      );

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
          update(state, updateObj({ modules: { $set: value.items } })));
        break;
      case 'seminarGroups':
        this.setState(state =>
          update(state, updateObj({ seminarGroups: { $set: value.items } })));
        break;
      case 'listOfUsercodes':
        this.setState(state =>
          update(state, updateObj({ listOfUsercodes: { $set: value } })));
        break;
      case 'staffRelationships':
        this.setState(state =>
          update(state, updateObj({ staffRelationships: { $set: value.items } })));
        break;
      default:
    }
  }

  isChecked(path) {
    return _.has(this.state, path);
  }

  selectDepartment({ target: { options, selectedIndex } }) {
    this.setState({ department: options[selectedIndex].text });
  }

  render() {
    const groupsInput = () => {
      const isWholeUni = this.isChecked('audience.university');
      const deptSelect = this.props.departments.length > 1 ?
        (<select
          defaultValue=""
          name="department"
          className="form-control"
          onClick={this.selectDepartment}
        >
          <option disabled hidden value=""/>
          {this.props.departments.map(dept => (
            <option key={dept[0]} value={dept[0]}>{dept[1]}</option>
          ))}
        </select>)
        : <input name="department" value={this.props.departments[0][0]} hidden readOnly/>;

      const pathPrefix = text => `audience.${isWholeUni ? 'university' : 'department'}${text}`;

      return (
        <div>
          {isWholeUni ? null : deptSelect}
          <RadioButton
            handleChange={this.handleChange}
            isChecked={this.isChecked(pathPrefix('.everyone'))}
            label={`Everyone in ${isWholeUni ?
              'the whole university' : this.state.department || '...'}`}
            name="everyone"
            btnGroup={pathPrefix('')}
          />
          <RadioButton
            handleChange={this.handleChange}
            isChecked={this.isChecked(pathPrefix('.groups'))}
            label={isWholeUni ? 'These groups:' : `Groups in ${this.state.department || '...'}`}
            name="groups"
            btnGroup={pathPrefix('')}
          >
            {Object.keys(GROUPS).map(key =>
              (<Checkbox
                key={key}
                handleChange={this.handleChange}
                isChecked={this.isChecked(pathPrefix(`.groups.${key}`))}
                label={GROUPS[key]}
                name={key}
                btnGroup={pathPrefix('.groups')}
              />),
            )}

            <Checkbox
              handleChange={this.handleChange}
              isChecked={this.isChecked(pathPrefix('.groups.UndergradStudents'))}
              label="Undergraduates"
              name="UndergradStudents"
              btnGroup={pathPrefix('.groups')}
            >
              <RadioButton
                handleChange={this.handleChange}
                isChecked={this.isChecked(pathPrefix('.groups.UndergradStudents.all'))}
                label="All"
                name="all"
                btnGroup={pathPrefix('.groups.UndergradStudents')}
              />
              <RadioButton
                handleChange={this.handleChange}
                isChecked={this.isChecked(pathPrefix('.groups.UndergradStudents.year'))}
                label="Choose year of study"
                name="year"
                btnGroup={pathPrefix('.groups.UndergradStudents')}
              >
                <Checkbox
                  handleChange={this.handleChange}
                  isChecked={this.isChecked(pathPrefix('.groups.UndergradStudents.year.first'))}
                  label="First year"
                  name="first"
                  btnGroup={pathPrefix('.groups.UndergradStudents.year')}
                />
                <Checkbox
                  handleChange={this.handleChange}
                  isChecked={this.isChecked(pathPrefix('.groups.UndergradStudents.year.second'))}
                  label="Second year"
                  name="second"
                  btnGroup={pathPrefix('.groups.UndergradStudents.year')}
                />
                <Checkbox
                  handleChange={this.handleChange}
                  isChecked={this.isChecked(pathPrefix('.groups.UndergradStudents.year.final'))}
                  label="Final year"
                  name="final"
                  btnGroup={pathPrefix('.groups.UndergradStudents.year')}
                />
              </RadioButton>
            </Checkbox>
            <Checkbox
              handleChange={this.handleChange}
              isChecked={this.isChecked(pathPrefix('.groups.modules'))}
              label="Students taking a particular module"
              name="modules"
              btnGroup={pathPrefix('.groups')}
            >
              <InputList
                name="modules"
                handleChange={this.handleChange}
                picker={modulePicker}
                items={_.get(this.state, pathPrefix('.groups.modules'), [])}
                placeholderText="Start typing to find a module"
              />
            </Checkbox>
            <Checkbox
              handleChange={this.handleChange}
              isChecked={this.isChecked(pathPrefix('.groups.seminarGroups'))}
              label="Students in a particular seminar group"
              name="seminarGroups"
              btnGroup={pathPrefix('.groups')}
            >
              <InputList
                name="seminarGroups"
                handleChange={this.handleChange}
                picker={seminarGroupPicker}
                items={_.get(this.state, pathPrefix('.groups.seminarGroups'), [])}
                placeholderText="Start typing to find a seminar group"
              />
            </Checkbox>
            <Checkbox
              handleChange={this.handleChange}
              isChecked={this.isChecked(pathPrefix('.groups.staffRelationships'))}
              label="Students of a member of staff"
              name="staffRelationships"
              btnGroup={pathPrefix('.groups')}
            >
              <InputOptionsList
                name="staffRelationships"
                handleChange={this.handleChange}
                picker={relationshipPicker}
                items={_.get(this.state, pathPrefix('.groups.staffRelationships'), [])}
                placeholderText="Start typing the name or usercode of the staff member"
              />
            </Checkbox>
            <Checkbox
              handleChange={this.handleChange}
              isChecked={this.isChecked(pathPrefix('.groups.listOfUsercodes'))}
              label="A list of people I'll type or paste in"
              name="listOfUsercodes"
              btnGroup={pathPrefix('.groups')}
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
        </div>);
    };

    console.log(this.state);
    return (
      <div>
        <div className="list-group">
          <label className="control-label">Who is this alert for?</label>

          {this.props.isGod ?
            <div>
              <RadioButton
                handleChange={this.handleChange}
                isChecked={this.isChecked('audience.university')}
                label="People across the whole university"
                name="university"
                btnGroup="audience"
              >
                {groupsInput()}
              </RadioButton>

              <RadioButton
                handleChange={this.handleChange}
                isChecked={this.isChecked('audience.department')}
                label="People within a particular department"
                name="department"
                btnGroup="audience"
              >
                {groupsInput()}
              </RadioButton>
            </div>
            : groupsInput()
          }
        </div>

        <div className="list-group">
          <label className="control-label">
            Is this alert specific to where people live ?
          </label>
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
    );
  }
}
