import React from 'react';
import ReactDOM from 'react-dom';
import ReactComponent from 'react/lib/ReactComponent';
import ReactTransitionGroup from 'react/lib/ReactTransitionGroup';

import * as tileElements from '../tiles';

import moment from 'moment';
import _ from 'lodash';

import jQuery from 'jquery';
import $ from 'jquery.transit';

import Immutable from 'immutable';
import { registerReducer } from '../../reducers';
import { connect } from 'react-redux';
import { fetchTileData } from '../../tiles'

const ZOOM_ANIMATION_DURATION = 500;

const TILE_ZOOM_IN = 'me.zoom-in';
const TILE_ZOOM_OUT = 'me.zoom-out';

function zoomInOn(tile) {
  return {
    type: TILE_ZOOM_IN,
    tile: tile.key
  };
}

function zoomOut() {
  return {
    type: TILE_ZOOM_OUT
  };
}

var tileZoomAnimating = false;

class MeView extends ReactComponent {

  constructor(props) {
    super(props);
    fetchTileData();
  }

  onTileClick(tile) {
    if (tile.href) {
      window.open(tile.href);
    } else if (!this.props.zoomedTile) {
      this.props.dispatch(zoomInOn(tile));
    }
  }

  onTileDismiss() {
    if (!tileZoomAnimating)
      this.props.dispatch(zoomOut());
  }

  renderTile(tile, zoomed = false) {
    let onTileClick = this.onTileClick.bind(this);

    let view = this;
    let baseTile = tileElements[tile.type];

    let props = _.merge({}, tile, {
      onClick(e) {
        onTileClick(tile, e);
      },
      view: this,
      zoomed: zoomed,
      key: zoomed ? tile.key + '-zoomed' : tile.key,
      ref: zoomed ? tile.key + '-zoomed' : tile.key,
      originalRef: tile.key,
      onDismiss: this.onTileDismiss.bind(this)
    });

    let element = class extends baseTile {
      componentWillEnter(callback) {
        view.componentWillEnter(props, callback);
      }

      componentWillLeave(callback) {
        view.componentWillLeave(props, callback);
      }
    };

    return React.createElement(element, props);
  }

  animateTileZoomOut(tileComponent, zoomComponent, callback) {
    let $tile = $(ReactDOM.findDOMNode(tileComponent.refs.tile.refs.tile)),
      $zoom = $(ReactDOM.findDOMNode(zoomComponent.refs.tile.refs.tile));

    let scaleX = $tile.outerWidth() / ($zoom.outerWidth() - 5);
    let scaleY = $tile.outerHeight() / $zoom.outerHeight();

    let x = $zoom.offset().left - $tile.offset().left;
    let y = $zoom.offset().top - $tile.offset().top;

    $tile.css({
      x: x,
      y: y,
      transformOriginX: 0,
      transformOriginY: 0,
      zIndex: 1001,
      scaleX: 1 / scaleX,
      scaleY: 1 / scaleY,
      opacity: 0,
      visibility: ''
    }).transition({
      x: 0,
      y: 0,
      scaleX: 1,
      scaleY: 1,
      opacity: 1
    }, ZOOM_ANIMATION_DURATION, function () {
      $tile.css({
        transformOriginX: '',
        transformOriginY: '',
        zIndex: ''
      });
    });

    $zoom.css({
      transformOriginX: 0,
      transformOriginY: 0,
      zIndex: 1002
    }).transition({
      x: -x,
      y: -y,
      scaleX: scaleX,
      scaleY: scaleY
    }, ZOOM_ANIMATION_DURATION, function () {
      tileZoomAnimating = false;
      callback();
    });
  }

  animateTileZoom(tileComponent, zoomComponent, callback) {
    let $tile = $(ReactDOM.findDOMNode(tileComponent.refs.tile.refs.tile)),
      $zoom = $(ReactDOM.findDOMNode(zoomComponent.refs.tile.refs.tile));

    $zoom.parent().show();

    let scaleX = $tile.outerWidth() / ($zoom.outerWidth() - 5);
    let scaleY = $tile.outerHeight() / $zoom.outerHeight();

    let x = $zoom.offset().left - $tile.offset().left;
    let y = $zoom.offset().top - $tile.offset().top;

    $tile.stop().css({
      transformOriginX: 0,
      transformOriginY: 0,
      zIndex: 99
    }).transition({
      x: x,
      y: y,
      scaleX: 1 / scaleX,
      scaleY: 1 / scaleY
    }, ZOOM_ANIMATION_DURATION, function () {
      $tile.css({
        zIndex: '',
        transformOriginX: '',
        transformOriginY: '',
        x: '',
        y: '',
        transform: '',
        visibility: 'hidden'
      });
      tileZoomAnimating = false;
      callback();
    });

    $zoom.stop().show().css({
      transformOriginX: 0,
      transformOriginY: 0,
      x: -x,
      y: -y,
      scaleX: scaleX,
      scaleY: scaleY,
      opacity: 0
    }).transition({
      x: 0,
      y: 0,
      scaleX: 1,
      scaleY: 1,
      opacity: 1
    }, ZOOM_ANIMATION_DURATION);
  }

  componentWillEnter(props, callback) {
    if (props.zoomed) {
      let tileComponent = this.refs.group.refs['.$' + props.originalRef];
      let zoomComponent = this.refs.group.refs['.$' + props.ref];

      tileZoomAnimating = true;

      $(ReactDOM.findDOMNode(zoomComponent)).hide();

      // have to do this otherwise the zoomComponent doesn't have its sizing information
      setTimeout(() => this.animateTileZoom(tileComponent, zoomComponent, callback), 0);
    } else {
      callback();
    }
  }

  componentWillLeave(props, callback) {
    if (props.zoomed) {
      let tileComponent = this.refs.group.refs['.$' + props.originalRef];
      let zoomComponent = this.refs.group.refs['.$' + props.ref];

      tileZoomAnimating = true;

      $(tileComponent.refs.tile.refs.tile).css({
        visibility: 'hidden'
      });

      // have to do this otherwise the tileComponent doesn't have its sizing information
      setTimeout(() => this.animateTileZoomOut(tileComponent, zoomComponent, callback), 0);
    } else {
      callback();
    }
  }

  renderTiles() {
    let zoomedTileKey = this.props.zoomedTile;

    let tiles = this.props.tiles.map((tile) => this.renderTile(tile));

    if (zoomedTileKey) {
      let zoomedTile = _.find(this.props.tiles, (tile) => tile.key == zoomedTileKey);
      tiles.push(this.renderTile(zoomedTile, true));
    }

    return (
      <div>
        { zoomedTileKey ?
          <div className="tile-zoom-backdrop" onClick={this.onTileDismiss.bind(this)}></div>
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

let initialState = Immutable.Map({
  zoomedTile: null
});

registerReducer('me', (state = initialState, action) => {
  switch (action.type) {
    case TILE_ZOOM_IN:
      return state.merge({
        zoomedTile: action.tile
      });
    case TILE_ZOOM_OUT:
      return state.merge({
        zoomedTile: null
      });
    default:
      return state;
  }
});

let select = (state) => ({
  zoomedTile: state.get('me').get('zoomedTile'),
  tiles: state.get('tiles').toJS()
});

export default connect(select)(MeView);