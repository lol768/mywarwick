import React from 'react';
import ReactDOM from 'react-dom';
import ReactComponent from 'react/lib/ReactComponent';
import ReactTransitionGroup from 'react/lib/ReactTransitionGroup';
import log from 'loglevel';
import { fetchTileContent } from '../../serverpipe';

import * as TILES from '../tiles';

import _ from 'lodash';

import $ from 'jquery.transit';

import Immutable from 'immutable';
import { connect } from 'react-redux';

import { registerReducer } from '../../reducers';

import * as tiles from '../../tiles';
import * as serverpipe from '../../serverpipe';

const ZOOM_ANIMATION_DURATION = 500;
export const EDITING_ANIMATION_DURATION = 700;

const TILE_ZOOM_IN = 'me.zoom-in';
const TILE_ZOOM_OUT = 'me.zoom-out';

let tileZoomAnimating = false;

function zoomInOn(tile) {
  return {
    type: TILE_ZOOM_IN,
    tile: tile.id,
  };
}

function zoomOut() {
  return {
    type: TILE_ZOOM_OUT,
  };
}

class MeView extends ReactComponent {

  constructor(props) {
    super(props);
    this.state = {};
    this.onTileDismiss = this.onTileDismiss.bind(this);
    this.boundOnBodyClick = this.onBodyClick.bind(this);
  }

  onTileExpand(tile) {
    if (tile.href) {
      window.open(tile.href);
    } else if (!this.props.zoomedTile) {
      this.props.dispatch(zoomInOn(tile));
    }
  }

  onTileDismiss() {
    if (!tileZoomAnimating) {
      this.props.dispatch(zoomOut());
    }
  }

  onBeginEditing(tile) {
    this.setState({
      editing: tile.id
    });

    let el = $(ReactDOM.findDOMNode(this));

    el.stop().transition({
      scale: 0.8
    }, EDITING_ANIMATION_DURATION, 'snap');

    // Ensure first release of the mouse button/finger is not interpreted as
    // exiting the editing mode
    $('body').one('mouseup touchend', () => {
      _.defer(() => $('body').on('click', this.boundOnBodyClick));
    });
  }

  onFinishEditing() {
    this.setState({
      editing: null
    });

    let el = $(ReactDOM.findDOMNode(this));

    el.stop().transition({
      scale: 1
    }, EDITING_ANIMATION_DURATION, 'snap', () => {
      el.removeAttr('style'); // transform creates positioning context
    });

    $('body').off('click', this.boundOnBodyClick);

    this.props.dispatch(serverpipe.persistTiles());
  }

  onBodyClick(e) {
    if (this.state.editing && $(e.target).parents('.tile--editing').length == 0) {
      this.onFinishEditing();
    }
  }

  renderTile(props, zoomed = false) {
    const tileComponent = TILES[props.type];
    if (tileComponent === undefined) {
      log.error(`No component available for tile type ${props.type}`);
      return null;
    }

    const id = props.id;
    const { content, errors, fetching } = this.props.tileContent[id] || {};
    const editing = this.state.editing === id;
    const ref = zoomed ? `${id}-zoomed` : id;

    const config = Object.assign({}, props, {
      zoomed,
      key: ref,
      ref,
      originalRef: id,
      content,
      errors,
      fetching,
      editing,
    });

    config.onDismiss = () => this.onTileDismiss();
    config.onExpand = () => this.onTileExpand(config);
    config.componentWillEnter = callback => this.componentWillEnter(config, callback);
    config.componentWillLeave = callback => this.componentWillLeave(config, callback);
    config.onClickRefresh = () => this.props.dispatch(fetchTileContent(id));
    config.onBeginEditing = () => this.onBeginEditing(config);
    config.onFinishEditing = () => this.onFinishEditing(config);
    config.onHide = () => this.onHideTile(config);
    config.onResize = () => this.onResizeTile(config);

    return React.createElement(tileComponent, config);
  }

  onHideTile(tile) {
    this.props.dispatch(tiles.hideTile(tile));

    this.onFinishEditing();
  }

  onResizeTile(tile) {
    const sizes = ['small', 'wide', 'large'];
    let nextSize = sizes[(sizes.indexOf(tile.size || tile.defaultSize) + 1) % sizes.length];

    this.props.dispatch(tiles.resizeTile(tile, nextSize));
  }

  animateTileZoomOut(tileComponent, zoomComponent, callback) {
    const $tile = $(ReactDOM.findDOMNode(tileComponent.refs.tile));
    const $zoom = $(ReactDOM.findDOMNode(zoomComponent.refs.tile));

    const scaleX = $tile.outerWidth() / ($zoom.outerWidth() - 5);
    const scaleY = $tile.outerHeight() / $zoom.outerHeight();

    const x = $zoom.offset().left - $tile.offset().left;
    const y = $zoom.offset().top - $tile.offset().top;

    $tile.stop().css({
      x: x,
      y: y,
      transformOriginX: 0,
      transformOriginY: 0,
      zIndex: 1001,
      scaleX: 1 / scaleX,
      scaleY: 1 / scaleY,
      opacity: 0,
      visibility: '',
    }).transition({
      x: 0,
      y: 0,
      scaleX: 1,
      scaleY: 1,
      opacity: 1,
    }, ZOOM_ANIMATION_DURATION, () => {
      $tile.css({
        transformOriginX: '',
        transformOriginY: '',
        zIndex: '',
      });
    });

    $zoom.stop().css({
      transformOriginX: 0,
      transformOriginY: 0,
      zIndex: 1002,
    }).transition({
      x: -x,
      y: -y,
      scaleX,
      scaleY,
    }, ZOOM_ANIMATION_DURATION, () => {
      tileZoomAnimating = false;
      callback();
    });
  }

  animateTileZoom(tileComponent, zoomComponent, callback) {
    const $tile = $(ReactDOM.findDOMNode(tileComponent.refs.tile));
    const $zoom = $(ReactDOM.findDOMNode(zoomComponent.refs.tile));

    $zoom.parent().show();

    const scaleX = $tile.outerWidth() / ($zoom.outerWidth() - 5);
    const scaleY = $tile.outerHeight() / $zoom.outerHeight();

    const x = $zoom.offset().left - $tile.offset().left;
    const y = $zoom.offset().top - $tile.offset().top;

    $tile.stop().css({
      transformOriginX: 0,
      transformOriginY: 0,
      zIndex: 99,
    }).transition({
      x,
      y,
      scaleX: 1 / scaleX,
      scaleY: 1 / scaleY,
    }, ZOOM_ANIMATION_DURATION, () => {
      $tile.css({
        zIndex: '',
        transformOriginX: '',
        transformOriginY: '',
        x: '',
        y: '',
        transform: '',
        visibility: 'hidden',
      });
      tileZoomAnimating = false;
      callback();
    });

    $zoom.stop().show().css({
      transformOriginX: 0,
      transformOriginY: 0,
      x: -x,
      y: -y,
      scaleX,
      scaleY,
      opacity: 0,
    }).transition({
      x: 0,
      y: 0,
      scaleX: 1,
      scaleY: 1,
      opacity: 1,
    }, ZOOM_ANIMATION_DURATION);
  }

  componentWillEnter(props, callback) {
    if (props.zoomed) {
      const tileComponent = this.refs.group.refs[`.$${props.originalRef}`];
      const zoomComponent = this.refs.group.refs[`.$${props.ref}`];

      tileZoomAnimating = true;

      $(ReactDOM.findDOMNode(zoomComponent)).hide();

      // have to do this otherwise the zoomComponent doesn't have its sizing information
      setTimeout(() => this.animateTileZoom(tileComponent, zoomComponent, callback), 0);
    } else {
      callback();
    }this.onTileDismiss = this.onTileDismiss.bind(this);
  }

  componentWillLeave(props, callback) {
    if (props.zoomed) {
      const tileComponent = this.refs.group.refs[`.$${props.originalRef}`];
      const zoomComponent = this.refs.group.refs[`.$${props.ref}`];

      tileZoomAnimating = true;

      $(tileComponent.refs.tile).css({
        visibility: 'hidden',
      });

      // have to do this otherwise the tileComponent doesn't have its sizing information
      setTimeout(() => this.animateTileZoomOut(tileComponent, zoomComponent, callback), 0);
    } else {
      callback();
    }
  }

  renderTiles() {
    const zoomedTileKey = this.props.zoomedTile;

    const tiles = this.props.tiles.map((tile) => this.renderTile(tile));

    if (zoomedTileKey) {
      const zoomedTile = _.find(this.props.tiles, (tile) => tile.id === zoomedTileKey);
      tiles.push(this.renderTile(zoomedTile, true));
    }

    return (
      <div className={this.state.editing ? 'me-view--editing' : ''}>
        { zoomedTileKey ?
          <div className="tile-zoom-backdrop" onClick={ this.onTileDismiss }></div>
          : null}
        <ReactTransitionGroup ref="group">
          {tiles}
        </ReactTransitionGroup>
      </div>
    );
  }

  render() {
    return <div className="row">{this.renderTiles()}</div>;
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
  tiles: state.get('tiles').get('items').filterNot(tile => tile.get('removed') === true).toJS(),
  tileContent: state.get('tileContent').toJS()
});

export default connect(select)(MeView);
