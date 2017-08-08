import React from 'react';
import * as PropTypes from 'prop-types';
import _ from 'lodash-es';
import { connect } from 'react-redux';
import wrapKeyboardSelect from '../../../keyboard-nav';
import * as colourSchemes from '../../../state/colour-schemes';
import HideableView from '../HideableView';

// import fetch from 'fetch-base64';

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
  };

  // image = [];

  static arrayBufferToBase64(buffer) {
    let binary = '';
    let bytes = [].slice.call(new Uint8Array(buffer));

    bytes.forEach((b) => binary += String.fromCharCode(b));

    return window.btoa(binary);
  };

  constructor(props) {
    super(props);
    this.onSelect = this.onSelect.bind(this);
    this.persist = this.persist.bind(this);
    // this.readImage = this.readImage.bind(this);
    // this.readImage()
    this.state = {};
    const basePath = `https://${window.location.hostname}/assets/images/`;
    this.props.schemes.forEach(scheme => {
      const url = `${basePath}${scheme.url}`;
      console.log('image url');
      console.log(url);
      fetch(url)
        .then(res => res.arrayBuffer())
        .then(ColourSchemesView.arrayBufferToBase64)
        .then(b64string => {
          const state = {};
          state[scheme.url] = b64string;
          this.setState(state);
        });
    });
  }

  onSelect(chosen) {
    if (!this.props.isOnline) return;
    this.persist(chosen);
  }

  persist(chosen) {
    this.props.dispatch(colourSchemes.changeColourScheme(chosen));
  }

  componentDidShow() {
    if (!this.props.isOnline) return;
    this.props.dispatch(colourSchemes.fetch());
  }

  makeInlineImageStyle(scheme) {
    const base64string = this.state[scheme.url];
    if (!base64string) return {};
    return {
      'background-image': `url(data:image/png;base64,${base64string})`,
    };
  }

  makeItem(scheme) {
    return (
      <div
        className={`list-group-item list-group-item--colour-scheme list-group-item__choice--colour-scheme`}
        role="button"
        key={scheme.id}
        onClick={e => wrapKeyboardSelect(() => this.onSelect(scheme.id), e)}
        onKeyUp={e => wrapKeyboardSelect(() => this.onSelect(scheme.id), e)}
        tabIndex={0}
        style={this.makeInlineImageStyle(scheme)}
      >
        <div className="media media-colour-scheme-choice">
          <div className="media media-colour-scheme-block">
            <div className="media-left">
              <div className="md-radio-colour-scheme-choice">
                <input
                  type="radio"
                  checked={scheme.id === this.props.chosen}
                  readOnly
                  disabled={!this.props.isOnline}
                />
                <label/>
              </div>
            </div>
            <div className="media-body media-body-colour-scheme-choice">
              {scheme.name}
            </div>
          </div>

          <div className="media-right"/>
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
    isOnline: store.device.isOnline,
  };
}

export default connect(select)(ColourSchemesView);
