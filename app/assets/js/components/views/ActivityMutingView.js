import React from 'react';
import * as PropTypes from 'prop-types';
import * as _ from 'lodash-es';
import { activityMuteDurations } from '../../state/notifications';
import CheckboxListGroupItem from '../ui/CheckboxListGroupItem';
import RadioListGroupItem from '../ui/RadioListGroupItem';

export default class ActivityMutingView extends React.Component {
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

  constructor(props) {
    super(props);
    this.state = {
      duration: null,
      formValues: {
        activityType: true,
        providerId: true,
      },
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
    this.setState({
      formValues: {
        ...this.state.formValues,
        [name]: !this.state.formValues[name],
      },
    });
  }

  saveMuting() {
    this.props.onMutingSave({
      ...this.state.formValues,
      duration: this.state.duration,
    });
  }

  renderForm() {
    return (
      <form className="form" id={ `muting-${this.props.id}-form` }>
        <div className="form-group">
          <label>Mute notifications about:</label>
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
          </div>
          {
            _.map(this.props.tags, tag => (
              <CheckboxListGroupItem
                key={tag.name}
                id={`tag-${tag.name}`}
                name={`tags[${tag.name}]`}
                value={tag.value}
                onClick={this.handleCheckboxChange}
                description={tag.display_value || tag.value}
                checked={this.state.formValues[tag.name]}
              />
            ))
          }
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
