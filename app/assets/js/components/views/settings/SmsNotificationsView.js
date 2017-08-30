import React from 'react';
import * as PropTypes from 'prop-types';
import HideableView from '../HideableView';
import * as smsNotifications from '../../../state/sms-notifications';
import classNames from 'classnames';
import { connect } from 'react-redux';
import ListGroupItem from '../../ui/ListGroupItem';
import NetworkAwareControl from '../../ui/NetworkAwareControl';
import Switch from '../../ui/Switch';
import wrapKeyboardSelect from '../../../keyboard-nav';
import ReactCSSTransitionGroup from 'react/lib/ReactCSSTransitionGroup';

class SmsNotificationsView extends HideableView {
  static propTypes = {
    dispatch: PropTypes.func.isRequired,
    fetching: PropTypes.bool.isRequired,
    fetched: PropTypes.bool.isRequired,
    failed: PropTypes.bool.isRequired,
    enabled: PropTypes.bool.isRequired,
    smsNumber: PropTypes.string,
    isOnline: PropTypes.bool.isRequired,
  };

  constructor(props) {
    super(props);

    this.onSwitchChange = this.onSwitchChange.bind(this);
    this.onEdit = this.onEdit.bind(this);
    this.onEditCancel = this.onEditCancel.bind(this);
    this.onEditSubmit = this.onEditSubmit.bind(this);
    this.handleJson = this.handleJson.bind(this);

    this.state = {
      enabled: props.enabled,
      smsNumber: props.smsNumber,
      editing: false,
      submitting: false,
      submitError: undefined,
      fromEmpty: false,
    };
  }

  componentDidShow() {
    if (!this.props.isOnline) return;
    this.props.dispatch(smsNotifications.fetch());
  }

  componentWillReceiveProps(nextProps) {
    this.setState({
      enabled: nextProps.enabled,
      smsNumber: nextProps.smsNumber,
    });
  }

  onSwitchChange(e) {
    wrapKeyboardSelect(() => {
      if (!this.state.enabled && !this.state.smsNumber) {
        this.setState({
          editing: true,
          fromEmpty: true,
        });
      } else {
        this.setState({
          enabled: !this.state.enabled,
        }, () => smsNotifications.persist(this.state.enabled, this.state.smsNumber));
      }
    }, e);
  }

  onEdit() {
    this.setState({
      editing: true,
    });
  }

  onEditCancel(e) {
    wrapKeyboardSelect(() =>
      this.setState({
        editing: false,
        fromEmpty: false,
        submitError: undefined,
      }),
    e);
  }

  onEditSubmit(e) {
    wrapKeyboardSelect(() =>
      this.setState({
        submitting: true,
      }, () => {
        smsNotifications.persist(
          this.state.fromEmpty || this.state.enabled,
          this.phoneNumberInput.value,
        )
          .then(response => response.json()).then(this.handleJson);
      }),
    e);
  }

  handleJson(json) {
    if (json.success) {
      this.setState({
        editing: false,
        submitting: false,
        submitError: undefined,
        fromEmpty: false,
        enabled: this.state.fromEmpty || this.state.enabled,
        smsNumber: this.phoneNumberInput.value,
      });
    } else if (json.errors.length > 0) {
      this.setState({
        submitting: false,
        submitError: json.errors[0].message || json.errors[0].id,
      });
    } else {
      this.setState({
        submitting: false,
        submitError: 'An unknown error occurred',
      });
    }
  }

  renderPhoneNumber() {
    return (
      <div>
        { this.state.smsNumber || 'None' }
        <i className="fa fa-fw fa-chevron-right" />
      </div>
    );
  }

  renderEdit() {
    return (
      <div>
        <div className="modal-backdrop fade in" />
        <div
          className="modal fade in"
          id="Settings:SMSNumber"
          tabIndex="-1"
          role="dialog"
        >
          <div className="modal-dialog" role="document">
            <div className="modal-content">
              <div className="modal-body">
                <form className="form" id="Settings:SMSNumber-form">
                  <div className={ classNames({
                    'form-group': true,
                    'has-error': this.state.submitError,
                  }) }
                  >
                    <label htmlFor="Settings:SMSNumber-input">
                      Phone number
                    </label>
                    <input
                      type="tel"
                      id="Settings:SMSNumber-input"
                      className="form-control"
                      placeholder="Phone number"
                      defaultValue={ this.state.smsNumber || '' }
                      ref={ (i) => { this.phoneNumberInput = i; } }
                    />
                    { this.state.submitError &&
                      <span className="help-block">{ this.state.submitError }</span>
                    }
                  </div>
                </form>
              </div>
              <div className="modal-footer">
                <button
                  type="button"
                  className="btn btn-default"
                  data-dismiss="modal"
                  onClick={ this.onEditCancel }
                  onKeyUp={ this.onEditCancel }
                >
                  Cancel
                </button>
                <button
                  type="button"
                  className="btn btn-primary"
                  onClick={ this.onEditSubmit }
                  onKeyUp={ this.onEditSubmit }
                  disabled={ this.state.submitting }
                >
                  Save
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    );
  }

  render() {
    return (
      <div>
        <div className="list-group fixed setting-colour-0">
          <div className="list-group-item">
            <div
              className="pull-right"
              onClick={ this.onSwitchChange }
              onKeyUp={ this.onSwitchChange }
              role="button"
              aria-disabled={ !this.props.isOnline }
              tabIndex={ this.props.isOnline ? 0 : -1 }
            >
              <NetworkAwareControl loading={ this.props.fetching } failure={ this.props.failed }>
                <Switch
                  id="Setting:WantsSms"
                  checked={ this.state.enabled }
                  disabled={ !this.props.isOnline }
                />
              </NetworkAwareControl>
            </div>
            <div className="list-group-item-heading">
              <h3>Copy my alerts to SMS</h3>
            </div>
          </div>
        </div>

        <div className="list-group setting-colour-0">
          <ListGroupItem
            icon="mobile"
            description="Phone number"
            failure={ this.props.failed }
            loading={ this.props.fetching }
            disabled={ !this.props.isOnline }
            uiControl={ this.renderPhoneNumber() }
            onClick={ this.onEdit }
          />
        </div>
        <ReactCSSTransitionGroup
          transitionName="grow-shrink-modal"
          transitionAppear
          transitionAppearTimeout={200}
          transitionEnter
          transitionEnterTimeout={200}
          transitionLeave
          transitionLeaveTimeout={200}
        >
          { this.state.editing && this.renderEdit() }
        </ReactCSSTransitionGroup>
      </div>
    );
  }
}

const select = state => ({
  enabled: state.smsNotifications.wantsSms,
  smsNumber: state.smsNotifications.smsNumber,
  fetching: state.smsNotifications.fetching,
  fetched: state.smsNotifications.fetched,
  failed: state.smsNotifications.failed,
  isOnline: state.device.isOnline,
});

export default connect(select)(SmsNotificationsView);
