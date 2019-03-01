import React from 'react';
import * as PropTypes from 'prop-types';
import ReactCSSTransitionGroup from 'react/lib/ReactCSSTransitionGroup';
import { connect } from 'react-redux';
import HideableView from '../HideableView';
import BootstrapModal from '../../ui/BootstrapModal';
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
      <BootstrapModal id="EAPDisableWarning">
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
      </BootstrapModal>
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
          that we&apos;re currently working on which may not be in their final form. Early access
          features appear for a limited period, so what you see will vary over time.
        </p>
        <p className="text--hint container-fluid">
          Joining the early access program will add a tile to the top of your current tiles to show
          what features are currently being tested, and to provide a channel for you to give us your
          feedback. This will help us to decide which features should be made available to everyone,
          which ones need adjustment, and which ones may not be useful enough to be worth releasing.
        </p>
        <p className="text--hint container-fluid">
          Touch the switch below if you&apos;d like to join the early access program. You can turn
          off early access at any time, and early access also turns itself off automatically after
          three months.
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

        <ReactCSSTransitionGroup
          transitionName="grow-shrink-modal"
          transitionAppear
          transitionAppearTimeout={200}
          transitionEnter
          transitionEnterTimeout={200}
          transitionLeave
          transitionLeaveTimeout={200}
        >
          { this.state.showDisableWarning && this.renderDisabledWarning() }
        </ReactCSSTransitionGroup>
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
