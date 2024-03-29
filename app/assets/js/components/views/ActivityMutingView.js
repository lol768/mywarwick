import React from 'react';
import * as PropTypes from 'prop-types';
import * as _ from 'lodash-es';
import { activityMuteDurations } from '../../state/notifications';
import BootstrapModal from '../ui/BootstrapModal';
import RadioListGroupItem from '../ui/RadioListGroupItem';
import wrapKeyboardSelect from '../../keyboard-nav';
import { lowercaseFirst } from '../../helpers';

// exported for tests
export const PublishNotificationType = 'mywarwick-user-publish-notification';
export const PROVIDER_SCOPE = 'providerId';
export const TYPE_SCOPE = 'activityType';

export default class ActivityMutingView extends React.PureComponent {
  static propTypes = {
    id: PropTypes.string.isRequired,
    provider: PropTypes.string.isRequired,
    providerDisplayName: PropTypes.string,
    providerOverrideMuting: PropTypes.bool.isRequired,
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
    isOnline: PropTypes.bool.isRequired,
  };

  constructor(props) {
    super(props);
    this.state = {
      duration: null,
      scope: this.isPublishNotification() ? PROVIDER_SCOPE : null,
    };
    this.handleDurationChange = this.handleDurationChange.bind(this);
    this.handleScopeChange = this.handleScopeChange.bind(this);
    this.saveMuting = this.saveMuting.bind(this);
    this.onMutingDismiss = this.onMutingDismiss.bind(this);
  }

  onMutingDismiss(e) {
    wrapKeyboardSelect(this.props.onMutingDismiss, e);
  }

  handleDurationChange(value) {
    this.setState({
      duration: value,
    });
  }

  handleScopeChange(value) {
    this.setState({
      scope: value,
    });
  }

  saveMuting(e) {
    wrapKeyboardSelect(() => {
      const nameValues = {
        activityType: (this.state.scope === 'activityType') ? this.props.activityType : null,
        providerId: this.props.provider, // mutes are always scoped to provider now
        duration: this.state.duration,
      };
      this.props.onMutingSave(nameValues);
    }, e);
  }

  isPublishNotification() {
    return this.props.activityType === PublishNotificationType;
  }

  renderScope() {
    return (
      <div className="form-group">
        <div className="list-group">
          <label>Mute:</label>
          <RadioListGroupItem
            id="activityType"
            name="scope"
            value={TYPE_SCOPE}
            onClick={this.handleScopeChange}
            description={`Just ‘${lowercaseFirst(this.props.activityTypeDisplayName || this.props.activityType)}’ alerts`}
            checked={this.state.scope === TYPE_SCOPE}
          />
          <RadioListGroupItem
            id="providerId"
            name="scope"
            value={PROVIDER_SCOPE}
            onClick={this.handleScopeChange}
            description={`All ${this.props.providerDisplayName || this.props.provider} alerts`}
            checked={this.state.scope === PROVIDER_SCOPE}
          />
        </div>
      </div>
    );
  }

  renderForm() {
    if (this.props.providerOverrideMuting) {
      return (
        <div>Alerts from {
          this.props.providerDisplayName || this.props.provider
        } cannot be muted</div>
      );
    }

    const isPublishNotification = this.isPublishNotification();
    const hintText = (
      <p className="text--hint">
        Muted alerts still appear in this list, but they don’t play a
        sound or appear on your phone’s lock screen when they’re delivered
      </p>
    );

    return (
      <form className="form" id={ `muting-${this.props.id}-form` }>
        { isPublishNotification ? null : this.renderScope() }
        { !isPublishNotification && hintText }
        <div className="list-group">
          <label>
            { this.isPublishNotification() ?
              `Mute alerts from ${this.props.providerDisplayName || this.props.provider} for:`
              : 'For:'
            }
          </label>
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
        { isPublishNotification && hintText }
      </form>
    );
  }

  render() {
    const someChecked = !!this.state.scope;

    return (
      <div className="activity-muting">
        <BootstrapModal id={`muting-${this.props.id}`}>
          {this.props.isOnline ?
            <div>
              <div className="modal-body">
                { this.renderForm() }
              </div>
              <div className="modal-footer">
                <button
                  type="button"
                  className="btn btn-default"
                  data-dismiss="modal"
                  onClick={ this.onMutingDismiss }
                  onKeyUp={ this.onMutingDismiss }
                >
                  Cancel
                </button>
                <button
                  type="button"
                  className="btn btn-primary"
                  onClick={ this.saveMuting }
                  onKeyUp={ this.saveMuting }
                  disabled={ !this.state.duration || !someChecked }
                >
                  Save changes
                </button>
              </div>
            </div>
            :
            <div>
              <div className="modal-body">
                You cannot mute an alert while your device is offline
              </div>
              <div className="modal-footer">
                <button
                  type="button"
                  className="btn btn-default"
                  data-dismiss="modal"
                  onClick={ this.onMutingDismiss }
                  onKeyUp={ this.onMutingDismiss }
                >
                  Close
                </button>
              </div>
            </div>
          }
        </BootstrapModal>
      </div>
    );
  }
}
