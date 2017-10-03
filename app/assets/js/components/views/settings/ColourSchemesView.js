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

        {/*<div className="list-group">
          <SwitchListGroupItem
            id="colourSchemeHighContrast"
            value=""
            checked={ this.props.isHighContrast }
            icon="adjust"
            description="High contrast"
            onClick={ this.onHighContrastToggle }
            disabled={ !this.props.isOnline }
          />
        </div>*/}

        <div className="list-group">
          {_.map(this.props.schemes, scheme => this.makeItem(scheme))}
        </div>
      </div>
    );
  }
}

function select(store) {
  return {
    fetching: store.colourSchemes.fetching,
    failed: store.colourSchemes.failed,
    fetched: store.colourSchemes.fetched,
    chosen: store.colourSchemes.chosen,
    schemes: store.colourSchemes.schemes,
    isHighContrast: store.colourSchemes.isHighContrast,
    isOnline: store.device.isOnline,
  };
}

export default connect(select)(ColourSchemesView);
