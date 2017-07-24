import React from 'react';
import * as PropTypes from 'prop-types';
import HideableView from '../HideableView';
import { connect } from 'react-redux';

class ColourSchemesView extends HideableView {

  static propTypes = {
    fetching: PropTypes.bool.isRequired,
    failed: PropTypes.bool.isRequired,
  };

  componentDidShow() {
    //this.props.dispatch(tiles.fetchTileContent());
  }

  render() {
    return (
      <div>
        <div className="list-group fixed setting-colour-0">
          <div className="list-group-item">
            <div className="list-group-item-heading">
              <h3>Colour schemes</h3>
            </div>
          </div>
        </div>

        <div className="list-group">
          <div
            className={ `list-group-item setting-colour-1` }
            role="button"
            tabIndex={0}
          >
            <div className="media">
              <div className="media-left">
                <i className={ `fa fa-fw fa-sun-o` }
                />
              </div>
              <div className={`media-body`}>
                Some media body!
              </div>
              <div className="media-right">
                <i className="fa fa-fw fa-chevron-right"/>
              </div>
            </div>
          </div>
        </div>
      </div>
    );
  }
}

function select(state) {
  return {
    fetching: state.colourSchemes.fetching,
    failed: state.colourSchemes.failed,
    fetched: state.colourSchemes.fetched,
  };
}

export default connect(select)(ColourSchemesView);
