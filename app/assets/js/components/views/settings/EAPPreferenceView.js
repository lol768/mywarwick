import React from 'react';
import * as PropTypes from 'prop-types';
import { connect } from 'react-redux';
import HideableView from '../HideableView';
import SwitchListGroupItem from '../../ui/SwitchListGroupItem';
import { toggleEnabled, fetch } from '../../../state/eap';

class EAPPreferenceView extends HideableView {
  static propTypes = {
    dispatch: PropTypes.func.isRequired,
    fetching: PropTypes.bool.isRequired,
    failed: PropTypes.bool.isRequired,
    fetched: PropTypes.bool.isRequired,
    enabled: PropTypes.bool.isRequired,
    isOnline: PropTypes.bool.isRequired,
  };

  constructor(props) {
    super(props);

    this.state = {
      showDisableWarning: false,
    };

    this.onEnableToggle = this.onEnableToggle.bind(this);
    this.onModalClose = this.onModalClose.bind(this);
  }

  componentDidShow() {
    if (!this.props.isOnline) return;
    this.props.dispatch(fetch());
  }

  componentWillReceiveProps(nextProps) {
    if (!nextProps.enabled && this.props.enabled) {
      this.setState({ showDisableWarning: true });
    } else {
      this.setState({ showDisableWarning: false });
    }
  }

  onEnableToggle() {
    this.props.dispatch(toggleEnabled(!this.props.enabled));
  }

  onModalClose() {
    this.setState({ showDisableWarning: false });
  }

  renderDisabledWarning() {
    return (
      <div>
        <div className="modal-backdrop fade in" />
        <div className="modal fade in" id="EAPDisableWarning" tabIndex="-1" role="dialog">
          <div className="modal-dialog" role="document">
            <div className="modal-content">
              <div className="modal-body">
                <p>
                  You have been removed from the early access program.
                </p>
                <p>
                  Any changes you made to your settings or tile layout while in the program
                  will <strong>not</strong> be reset unless you manually change them.
                </p>
              </div>
              <div className="modal-footer">
                <button
                  type="button"
                  className="btn btn-default"
                  data-dismiss="modal"
                  onClick={ this.onModalClose }
                  onKeyUp={ this.onModalClose }
                >
                  Close
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
        <div className="list-group setting-colour-0 fixed">
          <div className="list-group-item">
            <div className="list-group-item-heading">
              <h3>Early access program</h3>
            </div>
          </div>
        </div>

        <p className="text--hint container-fluid">
          Enrolling in the early access program means that you&apos;ll see tiles and other features
          that we&apos;re currently working on which may not be in their final form.
        </p>
        <p className="text--hint container-fluid">
          Your feedback on these features will help us to decide whether they should be made
          available to everyone, or if they need adjustment, or even, in some cases, whether they
          should not be released.
        </p>
        <p className="text--hint container-fluid">
          Touch the switch below if you would like to join the early access program.
        </p>

        <div className="list-group setting-colour-0">
          <SwitchListGroupItem
            id="eapEnable"
            value=""
            checked={ this.props.enabled }
            icon="eye"
            description="Early access program"
            onClick={ this.onEnableToggle }
            disabled={ !this.props.isOnline }
          />
        </div>

        { this.state.showDisableWarning && this.renderDisabledWarning() }
      </div>
    );
  }
}

function select(state) {
  return {
    fetching: state.eap.fetching,
    failed: state.eap.failed,
    fetched: state.eap.fetched,
    enabled: state.eap.enabled,
    isOnline: state.device.isOnline,
  };
}

export default connect(select)(EAPPreferenceView);
