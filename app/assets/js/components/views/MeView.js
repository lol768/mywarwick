import React from 'react';
import ReactDOM from 'react-dom';
import ReactComponent from 'react/lib/ReactComponent';
import ReactTransitionGroup from 'react/lib/ReactTransitionGroup';

import * as tileElements from '../tiles';

import moment from 'moment';
import _ from 'lodash';

import jQuery from 'jquery';
import $ from 'jquery.transit';

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

export default class MeView extends ReactComponent {

  constructor(props) {
    super(props);

    this.state = {
      zoomedTile: null
    };
  }

  onTileClick(tile) {
    if (tile.href) {
      window.open(tile.href);
    } else {
      this.setState({
        zoomedTile: this.state.zoomedTile ? null : tile
      });
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
    let state = tileComponent.refs.tile.state;

    let $tile = $(ReactDOM.findDOMNode(tileComponent)),
      $zoom = $(ReactDOM.findDOMNode(zoomComponent));

    let scaleX = state.naturalOuterWidth / $zoom.outerWidth();
    let scaleY = state.naturalOuterHeight / $zoom.outerHeight();

    $(document.body).css('overflow', 'hidden');

    $tile.css({
      x: -state.originalOffset.left,
      y: -state.originalOffset.top + $(window).scrollTop(),
      transformOriginX: 0,
      transformOriginY: 0,
      zIndex: 1001,
      scaleX: $(window).width() / state.naturalOuterWidth,
      scaleY: $(window).height() / state.naturalOuterHeight,
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
      $(document.body).css('overflow', '');
    });

    $zoom.css({
      zIndex: 1002
    }).transition({
      x: state.originalOffset.left,
      y: state.originalOffset.top - $(window).scrollTop(),
      scaleX: scaleX,
      scaleY: scaleY,
      opacity: 0
    }, ZOOM_ANIMATION_DURATION, callback);
  }

  animateTileZoom(tileComponent, zoomComponent, callback) {
    let state = tileComponent.refs.tile.state;

    let $tile = $(ReactDOM.findDOMNode(tileComponent)),
      $zoom = $(ReactDOM.findDOMNode(zoomComponent));

    let scaleX = state.naturalOuterWidth / $zoom.outerWidth();
    let scaleY = state.naturalOuterHeight / $zoom.outerHeight();

    $(document.body).css('overflow', 'hidden');

    $tile.stop().css({
      transformOriginX: 0,
      transformOriginY: 0,
      zIndex: 1001
    }).transition({
      x: -state.originalOffset.left,
      y: -state.originalOffset.top + $(window).scrollTop(),
      scaleX: $(window).width() / state.naturalOuterWidth,
      scaleY: $(window).height() / state.naturalOuterHeight,
      opacity: 0
    }, ZOOM_ANIMATION_DURATION, function () {
      $tile.css({
        zIndex: '',
        transformOriginX: '',
        transformOriginY: '',
        x: '',
        y: '',
        transform: '',
        opacity: ''
      });
      $(document.body).css('overflow', '');
      callback();
    });

    $zoom.stop().show().css({
      transformOriginX: 0,
      transformOriginY: 0,
      x: state.originalOffset.left,
      y: state.originalOffset.top - $(window).scrollTop(),
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
    let { zoomedTile } = this.state;

    let tiles = TILE_DATA.map((tile) => this.renderTile(tile));

    if (zoomedTile) {
      tiles.push(this.renderTile(zoomedTile, true));
    }

    return (
      <ReactTransitionGroup ref="group">
        {tiles}
      </ReactTransitionGroup>
    );
  }

  render() {
    return <div className="row">{this.renderTiles()}</div>;
  }

}
