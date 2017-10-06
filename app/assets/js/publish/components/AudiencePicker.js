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

const ELLIPSIS = '…';

export default class AudiencePicker extends React.PureComponent {
  static propTypes = {
    formData: PropTypes.object,
    isGod: PropTypes.bool,
    departments: PropTypes.object,
    deptSubsetOpts: PropTypes.object,
    locationOpts: PropTypes.object,
    store: PropTypes.object.isRequired,
  };

  static defaultProps = {
    formData: {},
    isGod: false,
    departments: {},
    deptSubsetOpts: {},
    locationOpts: {},
  };

  constructor(props) {
    super(props);

    if (!_.isEmpty(props.formData)) {
      const deptCode = props.formData.department;
      this.state = {
        ...props.formData,
        department: this.buildDeptObj(
          { [deptCode]: _.find(props.departments, (val, key) => key === deptCode) },
        ),
      };
    } else {
      this.state = {
        department: _.size(props.departments) > 1 ? ELLIPSIS : this.buildDeptObj(props.departments),
      };
    }

    this.handleChange = this.handleChange.bind(this);
    this.isChecked = this.isChecked.bind(this);
    this.selectDepartment = this.selectDepartment.bind(this);
    this.clearDepartment = this.clearDepartment.bind(this);
    this.groupsInput = this.groupsInput.bind(this);
    this.locationInput = this.locationInput.bind(this);
  }

  componentWillUpdate(nextProps, nextState) {
    this.props.store.dispatch({
      type: 'AUDIENCE_UPDATE',
      components: nextState,
    });
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
      case 'checkbox': {
        const existing = _.get(this.state, path, {});
        if (_.keys(existing).includes(value)) {
          const updated = _.pickBy(existing, (v, k) => k !== value);
          this.setState(state => _.set(_.cloneDeep(state), path, updated));
        } else {
          this.setState(state =>
            _.set(_.cloneDeep(state), path, _.assign({}, existing, { [value]: undefined })),
          );
        }
        break;
      }
      case 'radio':
        this.setState(state => _.set(_.cloneDeep(state), path, { [value]: undefined }));
        break;
      case 'Dept:Module':
      case 'Module':
        this.setState(state =>
          update(state, updateObj({ modules: { $set: value.items } })));
        break;
      case 'Dept:SeminarGroup':
      case 'SeminarGroup':
        this.setState(state =>
          update(state, updateObj({ seminarGroups: { $set: value.items } })));
        break;
      case 'listOfUsercodes':
        this.setState(state =>
          update(state, updateObj({ listOfUsercodes: { $set: value } })));
        break;
      case 'Dept:Relationship':
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

  buildDeptObj(dept) {
    const key = _.first(_.keys(dept));
    return { code: key, name: dept[key] };
  }

  selectDepartment(value) {
    this.setState({ department: { code: value, name: this.props.departments[value] } });
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
          {Object.keys(this.props.locationOpts).map(key =>
            (<Checkbox
              key={key}
              handleChange={(name, type, path) => this.handleChange(key, type, path)}
              formPath="locations.yesLocation"
              label={this.props.locationOpts[key]}
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
        defaultValue={this.state.department === ELLIPSIS ? '' : this.state.department.code}
        name="audience.department"
        className="form-control"
        onClick={({ currentTarget: { options, selectedIndex } }) =>
          this.selectDepartment(options[selectedIndex].value)
        }
      >
        <option disabled hidden value="">Select a department</option>
        {_.map(this.props.departments, (name, code) => (
          <option key={code} value={code}>{name}</option>
        ))}
      </select>)
      : (<input
        name="audience.department"
        value={Object.keys(this.props.departments)[0]}
        hidden
        readOnly
      />);

    const prefixPath = text => `audience.${isPublic ? 'universityWide' : 'department'}${text}`;
    const prefixDeptSubset = text => `${isPublic ? '' : 'Dept:'}${text}`;

    const groups = (
      <div>
        {_.map(this.props.deptSubsetOpts, (val, key) =>
          (<Checkbox
            key={key}
            handleChange={this.handleChange}
            isChecked={this.isChecked(prefixPath(`.groups.${prefixDeptSubset(key)}`))}
            label={val}
            name="audience.audience[]"
            value={prefixDeptSubset(key)}
            formPath={prefixPath('.groups')}
          />),
        )}
        <Checkbox
          handleChange={this.handleChange}
          isChecked={this.isChecked(prefixPath('.groups.modules'))}
          label="Students taking a particular module"
          value="modules"
          formPath={prefixPath('.groups')}
        >
          <InputList
            formPath={prefixPath('.groups.modules')}
            type={prefixDeptSubset('Module')}
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
            type={prefixDeptSubset('SeminarGroup')}
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
            type={prefixDeptSubset('Relationship')}
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
              placeholder="Type in usercodes, one per line"
              valuePrefix={prefixDeptSubset('')}
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
              label={`Everyone in ${_.get(this.state, 'department.name', ELLIPSIS)}`}
              value="Dept:All"
              name="audience.audience[]"
              formPath={prefixPath('')}
            />
            <RadioButton
              handleChange={this.handleChange}
              isChecked={this.isChecked(prefixPath('.groups'))}
              label={isPublic ?
                'These groups:' : `Groups in ${_.get(this.state, 'department.name', ELLIPSIS)}`}
              value="groups"
              formPath={prefixPath('')}
            >
              {groups}
            </RadioButton>
          </div>
      } </div>
    );
  }

  clearDepartment() {
    this.setState({ department: ELLIPSIS });
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
                onDeselect={this.clearDepartment}
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
