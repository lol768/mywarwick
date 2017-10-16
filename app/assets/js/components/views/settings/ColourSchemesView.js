import React from 'react';
import * as PropTypes from 'prop-types';
import _ from 'lodash-es';
import { connect } from 'react-redux';
import wrapKeyboardSelect from '../../../keyboard-nav';
import * as colourSchemes from '../../../state/colour-schemes';
import HideableView from '../HideableView';
import SwitchListGroupItem from '../../ui/SwitchListGroupItem';

class ColourSchemesView extends HideableView {
  static propTypes = {
    fetching: PropTypes.bool.isRequired,
    dispatch: PropTypes.func.isRequired,
    failed: PropTypes.bool.isRequired,
    fetched: PropTypes.bool.isRequired,
    chosen: PropTypes.number.isRequired,
    schemes: PropTypes.arrayOf(PropTypes.shape({
      id: PropTypes.number.isRequired,
      url: PropTypes.string.isRequired,
      name: PropTypes.string.isRequired,
    })).isRequired,
    isOnline: PropTypes.bool.isRequired,
    isHighContrast: PropTypes.bool.isRequired,
  };


  constructor(props) {
    super(props);
    this.onColourSchemeSelect = this.onColourSchemeSelect.bind(this);
    this.persist = this.persist.bind(this);
    this.onHighContrastToggle = this.onHighContrastToggle.bind(this);
  }

  onColourSchemeSelect(e) {
    wrapKeyboardSelect(() => {
      if (!this.props.isOnline) return;
      this.persist(parseInt(e.currentTarget.dataset.schemeid, 10));
    }, e);
  }

  persist(chosen) {
    this.props.dispatch(colourSchemes.changeColourScheme(chosen));
  }

  onHighContrastToggle() {
    if (!this.props.isOnline) return;
    this.props.dispatch(colourSchemes.toggleHighContrast(!this.props.isHighContrast));
  }

  componentDidShow() {
    if (!this.props.isOnline) return;
    this.props.dispatch(colourSchemes.fetch());
  }

  makeItem(scheme) {
    return (
      <div
        className={`list-group-item list-group-item--colour-scheme list-group-item__choice--colour-scheme list-group-item__choice--colour-scheme-${scheme.id}-preview`}
        role="button"
        key={ scheme.id }
        data-schemeid={ scheme.id }
        onClick={ this.onColourSchemeSelect }
        onKeyUp={ this.onColourSchemeSelect }
        tabIndex={ 0 }
      >
        <div className="media media-colour-scheme-choice">
          <div className="media media-colour-scheme-block">
            <div className="media-left">
              <div className="md-radio-colour-scheme-choice">
                <input
                  type="radio"
                  checked={ scheme.id === this.props.chosen }
                  readOnly
                  disabled={ !this.props.isOnline }
                />
                <label />
              </div>
            </div>
            <div className="media-body media-body-colour-scheme-choice">
              { scheme.name }
            </div>
          </div>

          <div className="media-right" />
        </div>
      </div>
    );
  }

  // Check whether the high contrast option is supported by the current app version.
  // Check is based on version number for now - see NEWSTART-1150 for how we'd like to
  // do this in future.
  isHighContrastSupported() {
    const { isNative, nativePlatform, nativeAppVersion } = this.props;

    if (!isNative) {
      return true;
    }

    switch (nativePlatform) {
      case 'android':
        return parseInt(nativeAppVersion, 10) >= 32;
      case 'ios':
        return parseInt(nativeAppVersion, 10) >= 3;
      default:
        return false;
    }
  }

  render() {
    return (
      <div>
        <div className="list-group fixed">
          <div className="list-group-item">
            <div className="list-group-item-heading">
              <h3>Colour scheme</h3>
            </div>
          </div>
        </div>

        { this.isHighContrastSupported() && <div className="list-group">
          <SwitchListGroupItem
            id="colourSchemeHighContrast"
            value=""
            checked={ this.props.isHighContrast }
            icon="adjust"
            description="High contrast"
            onClick={ this.onHighContrastToggle }
            disabled={ !this.props.isOnline }
          />
        </div> }

        <div className="list-group">
          {_.map(this.props.schemes, scheme => this.makeItem(scheme))}
        </div>
      </div>
    );
  }
}

function select(state) {
  const cs = state.colourSchemes;

  return {
    fetching: cs.fetching,
    failed: cs.failed,
    fetched: cs.fetched,
    chosen: cs.chosen,
    schemes: cs.schemes,
    isHighContrast: cs.isHighContrast,
    isOnline: state.device.isOnline,
    isNative: state.ui.native,
    nativePlatform: state.app.native.platform,
    nativeAppVersion: state.app.native.version,
  };
}

export default connect(select)(ColourSchemesView);
