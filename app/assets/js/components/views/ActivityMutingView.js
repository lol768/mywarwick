import React, { PropTypes } from 'react';
import * as _ from 'lodash-es';
import $ from 'jquery';
import { activityMuteDurations } from '../../state/notifications';

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
      someChecked: true,
    };
    this.handleDurationChange = this.handleDurationChange.bind(this);
    this.handleCheckboxChange = this.handleCheckboxChange.bind(this);
    this.saveMuting = this.saveMuting.bind(this);
  }

  handleDurationChange(event) {
    this.setState({
      duration: event.target.value,
    });
  }

  handleCheckboxChange() {
    this.setState({
      someChecked: $(`#muting-${this.props.id}-form`)
        .find('input[type="checkbox"]:checked').length > 0,
    });
  }

  saveMuting() {
    const formId = `#muting-${this.props.id}-form`;
    const formValues = $(formId).serializeArray();
    this.props.onMutingSave(formValues);
  }

  renderForm() {
    return (
      <form className="form" id={ `muting-${this.props.id}-form` }>
        <div className="form-group">
          <label>Don't send me notifications for:</label>
          <div className="checkbox">
            <label>
              <input
                type="checkbox"
                name="activityType"
                value={ this.props.activityType }
                defaultChecked
                onChange={ this.handleCheckboxChange }
              />
              { this.props.activityTypeDisplayName || this.props.activityType }
            </label>
          </div>
          <div className="checkbox">
            <label>
              <input
                type="checkbox"
                name="providerId"
                value={ this.props.provider }
                defaultChecked
                onChange={ this.handleCheckboxChange }
              />
              { this.props.providerDisplayName || this.props.provider }
            </label>
          </div>
          {
            _.map(this.props.tags, (tag) => (
              <div className="checkbox">
                <label>
                  <input
                    type="checkbox"
                    name={ `tags[${tag.name}]` }
                    value={ tag.value }
                    defaultChecked
                    onChange={ this.handleCheckboxChange }
                  />
                  { tag.display_value || tag.value }
                </label>
              </div>
            ))
          }
        </div>
        <div className="form-group">
          <label>For:</label>
          {
            _.map(activityMuteDurations, (duration) => (
              <div className="radio">
                <label>
                  <input
                    type="radio"
                    name="duration"
                    value={ duration.value }
                    onChange={ this.handleDurationChange }
                  />
                  { duration.displayValue }
                </label>
              </div>
            ))
          }
        </div>
      </form>
    );
  }

  render() {
    return (
      <div className="activity-muting">
        <div className="activity-muting__backdrop fade in"> </div>
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
                  disabled={ !this.state.duration || !this.state.someChecked }
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
