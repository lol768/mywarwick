import React from 'react';
import * as PropTypes from 'prop-types';
import * as _ from 'lodash-es';
import { activityMuteDurations } from '../../state/notifications';
import CheckboxListGroupItem from '../ui/CheckboxListGroupItem';
import RadioListGroupItem from '../ui/RadioListGroupItem';

const TagKeyPrefix = 'tag-';

export default class ActivityMutingView extends React.PureComponent {
  static propTypes = {
    id: PropTypes.string.isRequired,
    provider: PropTypes.string.isRequired,
    providerDisplayName: PropTypes.string,
    activityType: PropTypes.string.isRequired,
    activityTypeDisplayName: PropTypes.string,
    tags: PropTypes.arrayOf(PropTypes.shape({
      name: PropTypes.string.isRequired,
      display_name: PropTypes.string,
      value: PropTypes.string.isRequired,
      display_value: PropTypes.string,
    })),
    onMutingDismiss: PropTypes.func.isRequired,
    onMutingSave: PropTypes.func.isRequired,
  };

  static toTagKey(tag) {
    return `${TagKeyPrefix}${tag.name}-${tag.value}`;
  }

  constructor(props) {
    super(props);
    const formValues = {
      activityType: true,
      providerId: true,
    };
    _.forEach(props.tags, (tag) => {
      formValues[ActivityMutingView.toTagKey(tag)] = true;
    });
    this.state = {
      duration: null,
      formValues,
    };
    this.handleDurationChange = this.handleDurationChange.bind(this);
    this.handleCheckboxChange = this.handleCheckboxChange.bind(this);
    this.saveMuting = this.saveMuting.bind(this);
  }

  handleDurationChange(value) {
    this.setState({
      duration: value,
    });
  }

  handleCheckboxChange(value, name) {
    const keyName = (name.indexOf(TagKeyPrefix) === 0) ?
      ActivityMutingView.toTagKey({ name: name.replace(TagKeyPrefix, ''), value }) : name;
    this.setState({
      formValues: {
        ...this.state.formValues,
        [keyName]: !this.state.formValues[keyName],
      },
    });
  }

  saveMuting() {
    const nameValues = {
      activityType: (this.state.formValues.activityType) ? this.props.activityType : null,
      providerId: (this.state.formValues.providerId) ? this.props.provider : null,
      duration: this.state.duration,
    };
    _.forEach(this.props.tags, (tag) => {
      if (this.state.formValues[ActivityMutingView.toTagKey(tag)]) {
        nameValues[`tags[${tag.name}]`] = tag.value;
      }
    });
    this.props.onMutingSave(nameValues);
  }

  renderForm() {
    return (
      <form className="form" id={ `muting-${this.props.id}-form` }>
        <div className="form-group">
          <label>Mute alerts about:</label>
          <div className="list-group">
            <CheckboxListGroupItem
              id="activityType"
              name="activityType"
              value={this.props.activityType}
              onClick={this.handleCheckboxChange}
              description={this.props.activityTypeDisplayName || this.props.activityType}
              checked={this.state.formValues.activityType}
            />
            <CheckboxListGroupItem
              id="providerId"
              name="providerId"
              value={this.props.provider}
              onClick={this.handleCheckboxChange}
              description={this.props.providerDisplayName || this.props.provider}
              checked={this.state.formValues.providerId}
            />
            {
              _.map(this.props.tags, tag => (
                <CheckboxListGroupItem
                  key={tag.name}
                  id={`tag-${tag.name}`}
                  name={`tag-${tag.name}`}
                  value={tag.value}
                  onClick={this.handleCheckboxChange}
                  description={tag.display_value || tag.value}
                  checked={this.state.formValues[ActivityMutingView.toTagKey(tag)]}
                />
              ))
            }
          </div>
        </div>
        <div className="list-group">
          <label>For:</label>
          {
            _.map(activityMuteDurations, duration => (
              <RadioListGroupItem
                key={duration.value}
                description={duration.displayValue}
                value={duration.value}
                onClick={this.handleDurationChange}
                checked={this.state.duration === duration.value}
              />
            ))
          }
        </div>
      </form>
    );
  }

  render() {
    const someChecked = _.find(this.state.formValues, value => value === true) !== undefined;

    return (
      <div className="activity-muting">
        <div className="activity-muting__backdrop fade in" />
        <div
          className="activity-muting__modal fade in"
          id={`muting-${this.props.id}`}
          tabIndex="-1"
          role="dialog"
        >
          <div className="activity-muting__modal_dialog" role="document">
            <div className="activity-muting__modal_content">
              <div className="activity-muting__modal_body">
                { this.renderForm() }
              </div>
              <div className="activity-muting__modal_footer">
                <button
                  type="button"
                  className="btn btn-default"
                  data-dismiss="modal"
                  onClick={ this.props.onMutingDismiss }
                >
                  Cancel
                </button>
                <button
                  type="button"
                  className="btn btn-primary"
                  onClick={ this.saveMuting }
                  disabled={ !this.state.duration || !someChecked }
                >
                  Save changes
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    );
  }
}
