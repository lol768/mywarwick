import React from 'react';
import * as PropTypes from 'prop-types';
import HideableView from '../HideableView';
import * as smsNotifications from '../../../state/sms-notifications';
import classNames from 'classnames';
import { connect } from 'react-redux';
import BootstrapModal from '../../ui/BootstrapModal';
import ListGroupItem from '../../ui/ListGroupItem';
import NetworkAwareControl from '../../ui/NetworkAwareControl';
import Switch from '../../ui/Switch';
import wrapKeyboardSelect from '../../../keyboard-nav';
import ReactCSSTransitionGroup from 'react/lib/ReactCSSTransitionGroup';
import _ from 'lodash-es';

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
    this.onEditSubmitResend = this.onEditSubmitResend.bind(this);
    this.onEditSubmitCommon = this.onEditSubmitCommon.bind(this);
    this.handleJson = this.handleJson.bind(this);

    this.state = {
      enabled: props.enabled,
      smsNumber: props.smsNumber,
      editing: false,
      submitting: false,
      submitErrors: undefined,
      fromEmpty: false,
      verificationRequired: false,
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
      verificationRequired: false,
    });
  }

  onEditCancel(e) {
    wrapKeyboardSelect(() =>
      this.setState({
        editing: false,
        fromEmpty: false,
        submitErrors: undefined,
      }),
    e);
  }

  isWantsSms() {
    if (this.state.fromEmpty) {
      // We're here with the intention of enabling SMS notifications
      return true;
    }

    if (this.phoneNumberInput.value.trim().length === 0) {
      // The phone number field is empty
      return false;
    }

    return this.state.enabled;
  }

  onEditSubmitCommon(resend) {
    this.setState({
      submitting: true,
    }, () => {
      smsNotifications.persist(
        this.isWantsSms(),
        this.phoneNumberInput.value,
        (this.verificationCodeInput) ? this.verificationCodeInput.value : '',
        resend,
      )
        .then(response => response.json()).then(this.handleJson);
    });
  }

  onEditSubmit(e) {
    wrapKeyboardSelect(() => this.onEditSubmitCommon(false), e);
  }

  onEditSubmitResend(e) {
    wrapKeyboardSelect(() => this.onEditSubmitCommon(true), e);
  }

  handleJson(json) {
    if (json.success) {
      if (json.status === 'verificationRequired') {
        this.setState({
          submitting: false,
          submitErrors: undefined,
          verificationRequired: true,
        });
      } else {
        this.setState({
          editing: false,
          submitting: false,
          submitErrors: undefined,
          fromEmpty: false,
          enabled: this.isWantsSms(),
          smsNumber: this.phoneNumberInput.value,
        });
      }
    } else if (json.errors.length > 0) {
      const [verificationErrors, phoneErrors] = _.partition(json.errors, e => e.id === 'invalid-body-verification');
      const submitErrors = {};
      if (verificationErrors.length > 0) {
        submitErrors.verificationCode = verificationErrors[0].message || verificationErrors[0].id;
      }
      if (phoneErrors.length > 0) {
        submitErrors.phoneNumber = phoneErrors[0].message || phoneErrors[0].id;
      }
      this.setState({
        submitting: false,
        submitErrors,
        verificationRequired: json.status === 'verificationRequired',
      });
    } else {
      this.setState({
        submitting: false,
        submitErrors: { phoneNumber: 'An unknown error occurred' },
      });
    }
  }

  renderPhoneNumber() {
    return (
      <div>
        { this.state.smsNumber || 'None' }
        <i className="fal fa-fw fa-chevron-right" />
      </div>
    );
  }

  renderEdit() {
    return (
      <BootstrapModal id="Settings:SMSNumber">
        <div className="modal-body">
          <form className="form" id="Settings:SMSNumber-form" onSubmit={this.onEditSubmit}>
            <div className={ classNames({
              'form-group': true,
              'has-error': this.state.submitErrors && this.state.submitErrors.phoneNumber,
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
              { this.state.submitErrors && this.state.submitErrors.phoneNumber &&
              <span className="help-block">{ this.state.submitErrors.phoneNumber }</span>
              }
            </div>
            <p className="text--hint">
              We’ll use the number you provide only to send My Warwick alerts to, and we
              won’t share the number with any other application or with any third party.
              You can turn off SMS alerting at any time.
            </p>
            { this.state.verificationRequired &&
            <div>
              <div className={ classNames({
                'form-group': true,
                'has-error': this.state.submitErrors && this.state.submitErrors.verificationCode,
              }) }
              >
                <label htmlFor="Settings:SMSNumber-verificationCode">
                  We’ve sent you a code to verify your phone number. Enter it below.
                </label>
                <input
                  type="tel"
                  id="Settings:SMSNumber-verificationCode"
                  className="form-control"
                  placeholder="Verification code"
                  ref={ (i) => { this.verificationCodeInput = i; } }
                />
                { this.state.submitErrors && this.state.submitErrors.verificationCode &&
                <span className="help-block">{ this.state.submitErrors.verificationCode }</span>
                }
              </div>
              <button
                type="button"
                onClick={ this.onEditSubmitResend }
                className="btn btn-default"
              >
                Re-send verification code
              </button>
            </div>
            }
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
      </BootstrapModal>
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
