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

const ELLIPSIS = String.fromCharCode(8230);

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
    departments: PropTypes.object,
  };

  constructor(props) {
    super(props);

    if (!_.isEmpty(props.formData)) {
      const deptName = props.departments[props.formData.department] || '';
      this.state = {
        ...props.formData,
        department: deptName, // formData contains only department code, we need readable name
      };
    } else {
      const deptCodes = Object.keys(props.departments);
      this.state = {
        department: deptCodes.length > 1 ? ELLIPSIS : props.departments[deptCodes[0]],
      };
    }

    this.handleChange = this.handleChange.bind(this);
    this.isChecked = this.isChecked.bind(this);
    this.selectDepartment = this.selectDepartment.bind(this);
    this.groupsInput = this.groupsInput.bind(this);
    this.locationInput = this.locationInput.bind(this);
  }

  handleChange(value, type, path) {
    const updateObj = obj => (
      {
        audience: {
          [this.isChecked('audience.universityWide') ?
            'universityWide' : 'department']: { groups: obj },
        },
      }
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
      case 'Module':
        this.setState(state =>
          update(state, updateObj({ modules: { $set: value.items } })));
        break;
      case 'SeminarGroup':
        this.setState(state =>
          update(state, updateObj({ seminarGroups: { $set: value.items } })));
        break;
      case 'listOfUsercodes':
        this.setState(state =>
          update(state, updateObj({ listOfUsercodes: { $set: value } })));
        break;
      case 'Relationship':
        this.setState(state =>
          update(state, updateObj({ staffRelationships: { $set: value.items } })));
        break;
      default:
    }
  }

  isChecked(path) {
    return _.has(this.state, path);
  }

  selectDepartment(value) {
    this.setState({ department: value });
  }

  locationInput() {
    return (
      <div className="list-group">
        <label className="control-label">
          Is this alert specific to where people live ?&nbsp;
          <i
            className="fa fa-info-circle"
            data-toggle="tooltip"
            data-placement="left"
            title="Users can select one or more locations to receive
             alerts, or may choose not to specify any."
          />
        </label>
        <RadioButton
          handleChange={this.handleChange}
          isChecked={!this.isChecked('locations.yesLocation')}
          label="No"
          value="noLocation"
          formPath="locations"
        />
        <RadioButton
          handleChange={this.handleChange}
          isChecked={this.isChecked('locations.yesLocation')}
          label="Yes"
          value="yesLocation"
          formPath="locations"
        >
          {Object.keys(LOCATIONS).map(key =>
            (<Checkbox
              key={key}
              handleChange={(name, type, path) => this.handleChange(key, type, path)}
              formPath="locations.yesLocation"
              label={LOCATIONS[key]}
              name="audience.audience[]"
              value={`OptIn:Location:${key}`}
              isChecked={this.isChecked(`locations.yesLocation.${key}`)}
            />),
          )}
        </RadioButton>
      </div>
    );
  }
  groupsInput() {
    const isPublic = this.isChecked('audience.universityWide');
    const deptSelect = Object.keys(this.props.departments).length > 1 ?
      (<select
        defaultValue={this.state.department === ELLIPSIS ? '' : this.state.department}
        name="audience.department"
        className="form-control"
        onClick={({ target: { options, selectedIndex } }) =>
          this.selectDepartment(options[selectedIndex].text)}
      >
        <option disabled hidden value="">Select a department</option>
        {_.map(this.props.departments, (name, code) => (
          <option key={code} value={code}>{name}</option>
        ))}
      </select>)
      : (<input
        name="audience.audience[]"
        value={Object.keys(this.props.departments)[0]}
        hidden
        readOnly
      />);

    const prefixPath = text => `audience.${isPublic ? 'universityWide' : 'department'}${text}`;
    const prefixDeptSubset = text => `${isPublic ? '' : 'Dept:'}${text}`;

    const groups = (
      <div>
        {Object.keys(GROUPS).map(key =>
          (<Checkbox
            key={key}
            handleChange={this.handleChange}
            isChecked={this.isChecked(prefixPath(`.groups.${prefixDeptSubset(key)}`))}
            label={GROUPS[key]}
            name="audience.audience[]"
            value={prefixDeptSubset(key)}
            formPath={prefixPath('.groups')}
          />),
        )}
        <Checkbox
          handleChange={this.handleChange}
          isChecked={this.isChecked(prefixPath(`.groups.${prefixDeptSubset('UndergradStudents')}`))}
          label="Undergraduates"
          name="audience.audience[]"
          value={prefixDeptSubset('UndergradStudents')}
          formPath={prefixPath('.groups')}
        />
        <Checkbox
          handleChange={this.handleChange}
          isChecked={this.isChecked(prefixPath('.groups.modules'))}
          label="Students taking a particular module"
          value="modules"
          formPath={prefixPath('.groups')}
        >
          <InputList
            formPath={prefixPath('.groups.modules')}
            type="Module"
            name="audience.audience[]"
            handleChange={this.handleChange}
            picker={modulePicker}
            items={_.get(this.state, prefixPath('.groups.modules'), [])}
            placeholderText="Start typing to find a module"
          />
        </Checkbox>
        <Checkbox
          handleChange={this.handleChange}
          isChecked={this.isChecked(prefixPath('.groups.seminarGroups'))}
          label="Students in a particular seminar group"
          value="seminarGroups"
          formPath={prefixPath('.groups')}
        >
          <InputList
            formPath={prefixPath('.groups.seminarGroups')}
            type="SeminarGroup"
            handleChange={this.handleChange}
            name="audience.audience[]"
            picker={seminarGroupPicker}
            items={_.get(this.state, prefixPath('.groups.seminarGroups'), [])}
            placeholderText="Start typing to find a seminar group"
          />
        </Checkbox>
        <Checkbox
          handleChange={this.handleChange}
          isChecked={this.isChecked(prefixPath('.groups.staffRelationships'))}
          label="Students of a member of staff"
          value="staffRelationships"
          formPath={prefixPath('.groups')}
        >
          <InputOptionsList
            formPath={prefixPath('.groups.staffRelationships')}
            type="Relationship"
            name="audience.audience[]"
            handleChange={this.handleChange}
            picker={relationshipPicker}
            items={_.get(this.state, prefixPath('.groups.staffRelationships'), [])}
            placeholderText="Start typing the name or usercode of the staff member"
          />
        </Checkbox>
        <Checkbox
          handleChange={this.handleChange}
          isChecked={this.isChecked(prefixPath('.groups.listOfUsercodes'))}
          label="A list of people I'll type or paste in"
          value="listOfUsercodes"
          formPath={prefixPath('.groups')}
        >
          <div>
            <MultilineTextInput
              formPath={prefixPath('.groups.listOfUsercodes')}
              type="listOfUsercodes"
              name="audience.audience[]"
              handleChange={this.handleChange}
              items={_.get(this.state, prefixPath('.groups.listOfUsercodes'), [])}
              placeholder="Type in usercodes separated by commas (e.g. user1, user2)"
            />
          </div>
        </Checkbox>
      </div>
    );

    return (
      <div> {
        isPublic ?
          groups
          :
          <div>
            {deptSelect}
            <RadioButton
              handleChange={this.handleChange}
              isChecked={this.isChecked(prefixPath('.Dept:All'))}
              label={`Everyone in ${this.state.department || ELLIPSIS}`}
              value="Dept:All"
              name="audience.audience[]"
              formPath={prefixPath('')}
            />
            <RadioButton
              handleChange={this.handleChange}
              isChecked={this.isChecked(prefixPath('.groups'))}
              label={isPublic ? 'These groups:' : `Groups in ${this.state.department || ELLIPSIS}`}
              value="groups"
              formPath={prefixPath('')}
            >
              {groups}
            </RadioButton>
          </div>
      } </div>
    );
  }

  render() {
    return (
      <div>
        <div className="list-group">
          <label className="control-label">Who is this alert for?</label>

          {this.props.isGod ?
            <div>
              <RadioButton
                handleChange={this.handleChange}
                isChecked={this.isChecked('audience.universityWide')}
                label="People across the whole university"
                value="universityWide"
                formPath="audience"
              >
                {this.groupsInput()}
              </RadioButton>

              <RadioButton
                handleChange={this.handleChange}
                isChecked={this.isChecked('audience.department')}
                onDeselect={() => this.selectDepartment(undefined, undefined)}
                label="People within a particular department"
                value="department"
                formPath="audience"
              >
                {this.groupsInput()}
              </RadioButton>
            </div>
            : this.groupsInput()
          }
        </div>
        {this.locationInput()}
      </div>
    );
  }
}
