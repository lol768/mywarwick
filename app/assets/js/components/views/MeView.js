import React from 'react';
import ReactDOM from 'react-dom';
import ReactComponent from 'react/lib/ReactComponent';
import ReactCSSTransitionGroup from 'react/lib/ReactCSSTransitionGroup';
import log from 'loglevel';
import _ from 'lodash';
import $ from 'jquery.transit';
import Immutable from 'immutable';
import { connect } from 'react-redux';
import classNames from 'classnames';

import { registerReducer } from '../../reducers';
import Tile from '../tiles/Tile';
import * as TILE_TYPES from '../tiles';
import * as tiles from '../../tiles';
import * as serverpipe from '../../serverpipe';

const EDITING_ANIMATION_DURATION = 700;

import HiddenTile from '../tiles/HiddenTile';

const TILE_ZOOM_IN = 'me.zoom-in';
const TILE_ZOOM_OUT = 'me.zoom-out';

function zoomInOn(tile) {
  return {
    type: TILE_ZOOM_IN,
    tile: tile.id,
  };
}

export function zoomOut() {
  return {
    type: TILE_ZOOM_OUT,
  };
}

class MeView extends ReactComponent {

  constructor(props) {
    super(props);
    this.state = {};
    this.onTileDismiss = this.onTileDismiss.bind(this);
    this.onBodyClick = this.onBodyClick.bind(this);
  }

  onTileExpand(tile) {
    if (!this.props.zoomedTile) {
      this.props.dispatch(zoomInOn(tile));
    }
  }

  onTileDismiss() {
    this.props.dispatch(zoomOut());
  }

  onBeginEditing(tile) {
    this.setState({
      editing: tile.id,
    });

    const el = $(ReactDOM.findDOMNode(this));

    el.stop().transition({
      scale: 0.8,
    }, EDITING_ANIMATION_DURATION, 'snap');

    // Ensure first release of the mouse button/finger is not interpreted as
    // exiting the editing mode
    $('body').one('mouseup touchend', () => {
      _.defer(() => $('body').on('click', this.onBodyClick));
    });
  }

  onFinishEditing() {
    this.setState({
      editing: null,
    });

    const el = $(ReactDOM.findDOMNode(this));

    el.stop().transition({
      scale: 1,
    }, EDITING_ANIMATION_DURATION, 'snap', () => {
      el.removeAttr('style'); // transform creates positioning context
    });

    $('body').off('click', this.onBodyClick);

    this.props.dispatch(serverpipe.persistTiles());
  }

  onBodyClick(e) {
    if (this.state.editing && $(e.target).parents('.tile--editing').length === 0) {
      _.defer(() => this.onFinishEditing());
    }
  }

  renderTile(props, zoomed = false) {
    const tileContentComponent = TILE_TYPES[props.type];
    const isDesktop = this.props.layoutClassName === 'desktop';
    if (tileContentComponent === undefined) {
      log.error(`No component available for tile type ${props.type}`);
      return null;
    }

    const id = props.id;
    const { content, errors, fetching, fetchedAt } = this.props.tileContent[id] || {};
    const editing = this.state.editing === id;
    const ref = zoomed ? `${id}-zoomed` : id;

    const config = Object.assign({}, props, {
      zoomed,
      canZoom: tileContentComponent.canZoom(content),
      key: ref,
      ref,
      originalRef: id,
      content,
      errors,
      fetching,
      fetchedAt,
      editing,
      editingAny: !!this.state.editing,
      isDesktop,
    });

    // Zooming
    config.onZoomIn = () => this.onTileExpand(config);
    config.onZoomOut = () => this.onTileDismiss();

    // Editing
    config.onBeginEditing = () => this.onBeginEditing(config);
    config.onHide = () => this.onHideTile(config);
    config.onResize = () => this.onResizeTile(config);
    config.editAnimationDuration = EDITING_ANIMATION_DURATION;

    // subset of config needed by TileContent subclasses
    const contentConfig = {
      content,
      zoomed,
      editingAny: config.editingAny,
      fetchedAt,
    };

    return (
      <Tile { ... config }>
        { React.createElement(tileContentComponent, contentConfig) }
      </Tile>
    );
  }

  onHideTile(tile) {
    this.props.dispatch(tiles.hideTile(tile));

    this.onFinishEditing();
  }

  onResizeTile(tile) {
    const sizes = ['small', 'wide', 'large'];
    const nextSize = sizes[(sizes.indexOf(tile.size || tile.defaultSize) + 1) % sizes.length];

    this.props.dispatch(tiles.resizeTile(tile, nextSize));
  }

  onShowTile(tile) {
    this.props.dispatch(tiles.showTile(tile));

    this.props.dispatch(serverpipe.persistTiles());
    this.props.dispatch(serverpipe.fetchTileContent(tile.id));

    this.onFinishEditing();
  }

  renderTiles() {
    const { editing } = this.state;
    const tileComponents = this.props.tiles.map((tile) => this.renderTile(tile));
    const hiddenTiles = this.props.hiddenTiles.map(
      tile => <HiddenTile key={ tile.id } {...tile} onShow={() => this.onShowTile(tile)}/> // eslint-disable-line react/jsx-no-bind, max-len
    );

    // Show hidden tiles (if any) when editing, or if there are no visible tiles
    const showHiddenTiles = hiddenTiles.length > 0 && (editing || tileComponents.length === 0);

    return (
      <div>
        <div>
          {tileComponents}
        </div>
        { showHiddenTiles ?
          <div>
            <div style={{ clear: 'both' }}></div>
            <div>
              <h3 style={{ marginTop: 30 }}>More tiles</h3>
              <div>
                {hiddenTiles}
              </div>
            </div>
          </div>
          : null }
      </div>
    );
  }

  render() {
    const classes = classNames('row', 'me-view', { 'me-view--editing': this.state.editing });
    const isDesktop = this.props.layoutClassName === 'desktop';
    const transitionProps = {
      transitionName: 'slider',
      transitionEnterTimeout: 300,
      transitionLeaveTimeout: 300,
      transitionEnter: !isDesktop,
      transitionLeave: !isDesktop,
    };

    return (
      <div className={classes}>
        { this.props.zoomedTile && isDesktop ?
          <div className="tile-zoom-backdrop" onClick={ this.onTileDismiss }></div>
          : null}
        {this.renderTiles()}
        <ReactCSSTransitionGroup {...transitionProps}>
          {this.props.zoomedTile ?
            <div>
              {this.renderTile(
                _.find(this.props.tiles, (tile) => tile.id === this.props.zoomedTile), true
              )}
            </div>
            : null }
        </ReactCSSTransitionGroup>
      </div>
    );
  }
}

const initialState = Immutable.Map({
  zoomedTile: null,
});

registerReducer('me', (state = initialState, action) => {
  switch (action.type) {
    case TILE_ZOOM_IN:
      return state.merge({
        zoomedTile: action.tile,
      });
    case TILE_ZOOM_OUT:
      return state.merge({
        zoomedTile: null,
      });
    default:
      return state;
  }
});

const select = (state) => ({
  zoomedTile: state.get('me').get('zoomedTile'),
  layoutClassName: state.get('ui').get('className'),
  tiles: state.get('tiles').get('items').filterNot(tile => tile.get('removed')).toJS(),
  hiddenTiles: state.get('tiles').get('items').filter(tile => tile.get('removed')).toJS(),
  tileContent: state.get('tileContent').toJS(),
});

export default connect(select)(MeView);
