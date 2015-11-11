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

const ZOOM_ANIMATION_DURATION = 500;

let TILE_DATA = [
  {
    key: 'mail',
    type: 'list',
    title: 'Mail',
    href: 'http://warwick.ac.uk/mymail',
    backgroundColor: '#0078d7',
    icon: 'envelope-o',
    word: 'unread',
    size: 'wide',
    items: [
      {
        key: 1,
        title: 'Christelle Evaert',
        text: 'Departmental meeting cancelled',
        date: moment().subtract(1, 'hour').toISOString()
      },
      {
        key: 2,
        title: 'IT Service Desk',
        text: 'Emergency RFC',
        date: moment().subtract(4, 'hour').toISOString()
      },
      {
        key: 3,
        title: 'Linda Squirrel',
        text: 'IT Induction Day reminder',
        date: moment().subtract(26, 'hour').toISOString()
      }
    ]
  },
  {
    key: 'tabula',
    type: 'text',
    title: 'Tabula',
    href: 'https://tabula.warwick.ac.uk',
    backgroundColor: '#239b92',
    icon: 'cog',
    items: [
      {
        key: 1,
        callout: 3,
        text: 'actions required'
      }
    ]
  },
  {
    key: 'live-departures',
    type: 'text',
    title: 'Live Departures',
    backgroundColor: '#ef4050',
    icon: 'bus',
    items: [
      {
        key: 1,
        callout: '17:52',
        text: 'U1 to Leamington'
      },
      {
        key: 2,
        callout: '18:01',
        text: '11 to Coventry'
      }
    ]
  },
  {
    key: 'modules',
    type: 'count',
    word: 'modules this term',
    title: 'My Modules',
    icon: 'mortar-board',
    items: [
      {
        key: 1,
        href: 'http://warwick.ac.uk/cs118',
        text: 'CS118 Programming for Computer Scientists'
      },
      {
        key: 2,
        href: 'http://warwick.ac.uk/cs256',
        text: 'CS256 Functional Programming'
      },
      {
        key: 3,
        href: 'http://warwick.ac.uk/cs324',
        text: 'CS324 Computer Graphics'
      }
    ]
  }
];

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

class MeView extends ReactComponent {

  constructor(props) {
    super(props);
  }

  onTileClick(tile) {
    if (tile.href) {
      window.open(tile.href);
    } else {
      this.props.dispatch(this.props.zoomedTile ? zoomOut() : zoomInOn(tile));
    }
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
      originalRef: tile.key
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
      opacity: 0
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
    }, ZOOM_ANIMATION_DURATION, callback);
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
        transform: ''
      });
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

      // have to do this otherwise the tileComponent doesn't have its sizing information
      setTimeout(() => this.animateTileZoomOut(tileComponent, zoomComponent, callback), 0);
    } else {
      callback();
    }
  }

  renderTiles() {
    let zoomedTileKey = this.props.zoomedTile;

    let tiles = TILE_DATA.map((tile) => this.renderTile(tile));

    if (zoomedTileKey) {
      let zoomedTile = _.find(TILE_DATA, (tile) => tile.key == zoomedTileKey);
      tiles.push(this.renderTile(zoomedTile, true));
    }

    return (
      <div>
        { zoomedTileKey ?
          <div className="tile-zoom-backdrop" onClick={() => this.props.dispatch(zoomOut())}></div>
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

let select = (state) => ({zoomedTile: state.get('me').get('zoomedTile')});

export default connect(select)(MeView);
