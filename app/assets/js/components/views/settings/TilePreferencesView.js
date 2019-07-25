import React from 'react';
import _ from 'lodash-es';
import { connect } from 'react-redux';
import { push } from 'react-router-redux';
import * as PropTypes from 'prop-types';
import { Routes } from '../../AppRoot';
import HideableView from '../HideableView';
import * as tiles from '../../../state/tiles';
import wrapKeyboardSelect from '../../../keyboard-nav';

class TilePreferencesView extends HideableView {
  static propTypes = {
    isOnline: PropTypes.bool.isRequired,
    dispatch: PropTypes.func.isRequired,
    tiles: PropTypes.arrayOf(PropTypes.shape({
      id: PropTypes.string.isRequired,
      colour: PropTypes.number.isRequired,
      icon: PropTypes.string.isRequired,
      title: PropTypes.string.isRequired,
    })),
  };

  constructor(props) {
    super(props);
    this.onTile = this.onTile.bind(this);
  }

  componentDidShow() {
    this.props.dispatch(tiles.fetchTileContent());
  }

  onTile(e) {
    wrapKeyboardSelect(() => {
      this.props.dispatch(
        push(`/${Routes.SETTINGS}/${Routes.SettingsRoutes.TILES}/${e.currentTarget.dataset.tileid}`),
      );
    }, e);
  }

  render() {
    return (
      <div>
        <div className="list-group fixed setting-colour-0">
          <div className="list-group-item">
            <div className="list-group-item-heading">
              <h3>Tile preferences</h3>
            </div>
          </div>
        </div>

        <div className="list-group">
          { _.map(_.sortBy(this.props.tiles, 'title'), tile => (<div
              key={ tile.id }
              className={ `list-group-item setting-colour-${tile.colour}` }
              role="button"
              tabIndex={0}
              data-tileid={ tile.id }
              onClick={ this.onTile }
              onKeyUp={ this.onTile }
            >
              <div className="media">
                <div className="media-left">
                  <i className={ `fal fa-fw fa-${
                    (tile.id === 'weather') ? 'sun-o' : tile.icon
                  }` }
                  />
                </div>
                <div className={`media-body${this.props.isOnline ? '' : ' media-body-disabled'}`}>
                  { tile.title }
                </div>
                <div className="media-right">
                  <i className="fal fa-fw fa-chevron-right" />
                </div>
              </div>
            </div>))
          }
        </div>
      </div>
    );
  }
}

function select(state) {
  const tilesWithPreferences = _.keys(_.pickBy(
    state.tiles.data.options,
    options => _.keys(options).length > 0,
  ));
  return {
    isOnline: state.device.isOnline,
    tiles: _.filter(state.tiles.data.tiles, tile => _.includes(tilesWithPreferences, tile.id)),
  };
}

export default connect(select)(TilePreferencesView);
