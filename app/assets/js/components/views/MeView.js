import React from 'react';
import ReactComponent from 'react/lib/ReactComponent';
import ReactCSSTransitionGroup from 'react/lib/ReactCSSTransitionGroup';
import ReactGridLayoutBase, { WidthProvider } from 'react-grid-layout';

import _ from 'lodash';
import $ from 'jquery.transit';
import { connect } from 'react-redux';
import classNames from 'classnames';
import { goBack } from 'react-router-redux';

import * as tiles from '../../tiles';
import * as serverpipe from '../../serverpipe';
import { TILE_SIZES } from '../tiles/TileContent';
import TileView from './TileView';

import HiddenTile from '../tiles/HiddenTile';

const ReactGridLayout = WidthProvider(ReactGridLayoutBase); // eslint-disable-line new-cap

function getSizeFromSizeName(name) {
  const sizes = {
    [TILE_SIZES.SMALL]: { width: 1, height: 1 },
    [TILE_SIZES.WIDE]: { width: 2, height: 1 },
    [TILE_SIZES.LARGE]: { width: 2, height: 2 },
  };

  return sizes[name];
}

function getSizeNameFromSize(size) {
  const { width, height } = size;

  if (width === 1 && height === 1) {
    return TILE_SIZES.SMALL;
  }

  if (width === 2 && height === 1) {
    return TILE_SIZES.WIDE;
  }

  return TILE_SIZES.LARGE;
}

class MeView extends ReactComponent {

  constructor(props) {
    super(props);
    this.state = {};
    this.onBodyClick = this.onBodyClick.bind(this);
    this.onTileDismiss = this.onTileDismiss.bind(this);
    this.onLayoutChange = this.onLayoutChange.bind(this);
    this.onTouchMove = this.onTouchMove.bind(this);
    this.onDragStart = this.onDragStart.bind(this);
    this.onDragStop = this.onDragStop.bind(this);
  }

  onBeginEditing(tile) {
    this.setState({
      editing: tile.id,
    });

    /*
    const el = $(ReactDOM.findDOMNode(this));

    el.stop().transition({
      scale: 0.8,
    }, EDITING_ANIMATION_DURATION, 'snap');
    */

    // Ensure first release of the mouse button/finger is not interpreted as
    // exiting the editing mode
    $('body').one('mouseup touchend', () => {
      _.defer(() => {
        $('body').off('click', this.onBodyClick).on('click', this.onBodyClick);
      });
    });
  }

  onFinishEditing() {
    if (!this.unmounted) {
      this.setState({
        editing: null,
      });
    }

    /*
    const el = $(ReactDOM.findDOMNode(this));

    el.stop().transition({
      scale: 1,
    }, EDITING_ANIMATION_DURATION, 'snap', () => {
      el.removeAttr('style'); // transform creates positioning context
    });
    */

    $('body').off('click', this.onBodyClick);

    this.props.dispatch(serverpipe.persistTiles());
  }

  onBodyClick(e) {
    if (this.state.editing && $(e.target).parents('.tile').length === 0) {
      _.defer(() => this.onFinishEditing());
    }
  }

  onDragStart(layout, item) {
    this.dragging = true;
    this.onBeginEditing({ id: item.i });
  }

  onDragStop() {
    this.dragging = false;
  }

  componentDidMount() {
    $(window).on('touchmove', this.onTouchMove);
  }

  componentWillUnmount() {
    $(window).off('touchmove', this.onTouchMove);
    this.unmounted = true;
  }

  onTouchMove(e) {
    if (this.dragging) {
      e.preventDefault();
    }
  }

  getTileLayout(layout) {
    return layout
      .filter(tile => tile.layoutWidth === this.props.layoutWidth)
      .map(item => ({
        i: item.tile,
        x: item.x,
        y: item.y,
        w: item.width,
        h: item.height,
      }));
  }

  onLayoutChange(layout) {
    if (this.previousLayout === undefined || !_.isEqual(layout, this.previousLayout)) {
      this.props.dispatch(tiles.tileLayoutChange(layout, this.props.layoutWidth));
      this.previousLayout = _.cloneDeep(layout);
    }
  }

  renderTile(props) {
    const { id } = props;

    return (
      <TileView
        ref={id}
        key={id}
        id={id}
        view={this}
        editing={this.state.editing === id}
        editingAny={!!this.state.editing}
        size={this.getTileSize(id)}
      />
    );
  }

  onHideTile(tile) {
    this.props.dispatch(tiles.hideTile(tile));

    this.onFinishEditing();
  }

  onResizeTile(tile) {
    const sizes = _.values(TILE_SIZES);
    const nextSize = sizes[(sizes.indexOf(tile.size || tile.defaultSize) + 1) % sizes.length];

    const { width, height } = getSizeFromSizeName(nextSize);

    this.props.dispatch(tiles.resizeTile(tile, this.props.layoutWidth, width, height));
  }

  onShowTile(tile) {
    this.props.dispatch(tiles.showTile(tile));

    this.props.dispatch(serverpipe.persistTiles());
    this.props.dispatch(serverpipe.fetchTileContent(tile.id));

    this.onFinishEditing();
  }

  getTileSize(id) {
    const layout = this.props.layout.filter(i =>
      i.tile === id && i.layoutWidth === this.props.layoutWidth
    )[0];

    if (!layout) {
      return TILE_SIZES.SMALL;
    }

    return getSizeNameFromSize(layout);
  }

  renderTiles() {
    const { editing } = this.state;
    const tileComponents = this.props.tiles.map((tile) => this.renderTile(tile));
    const hiddenTiles = this.props.hiddenTiles.map(
      tile => <HiddenTile key={ tile.id } {...tile} onShow={() => this.onShowTile(tile)} /> // eslint-disable-line react/jsx-no-bind, max-len
    );

    // Show hidden tiles (if any) when editing, or if there are no visible tiles
    const showHiddenTiles = hiddenTiles.length > 0 && (editing || tileComponents.length === 0);

    const layout = this.getTileLayout(this.props.layout, this.props.layoutWidth);

    return (
      <div>
        <ReactGridLayout
          layout={layout}
          isDraggable={!!editing}
          isResizable={false}
          cols={this.props.layoutWidth}
          rowHeight={125}
          margin={[4, 4]}
          useCSSTransformations
          onLayoutChange={this.onLayoutChange}
          verticalCompact
          draggableCancel=".tile__edit-control"
          onDragStart={this.onDragStart}
          onDragStop={this.onDragStop}
        >
          {tileComponents.map(component =>
            <div
              key={component.props.id}
              className={this.state.editing === component.props.id ? 'react-grid-item--editing' : ''} // eslint-disable-line max-len
            >
              {component}
            </div>)}
        </ReactGridLayout>
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

  onTileDismiss() {
    this.props.dispatch(goBack());
  }

  render() {
    const classes = classNames('me-view', { 'me-view--editing': this.state.editing });
    const { isDesktop } = this.props;
    const transitionProps = {
      transitionName: 'slider',
      transitionEnterTimeout: 300,
      transitionLeaveTimeout: 300,
      transitionEnter: !isDesktop,
      transitionLeave: !isDesktop,
    };

    return (
      <div className={classes}>
        { this.props.children && isDesktop ?
          <div className="tile-zoom-backdrop" onClick={ this.onTileDismiss }></div>
          : null}
        {this.renderTiles()}
        <ReactCSSTransitionGroup {...transitionProps}>
          { this.props.children }
        </ReactCSSTransitionGroup>
      </div>
    );
  }
}

const select = (state) => {
  const items = state.getIn(['tiles', 'data', 'tiles']);

  return {
    isDesktop: state.getIn(['ui', 'className']) === 'desktop',
    layoutWidth: state.getIn(['ui', 'isFourColumnLayout']) === true ? 4 : 2,
    tiles: items.filterNot(tile => tile.get('removed')).toJS(),
    hiddenTiles: items.filter(tile => tile.get('removed')).toJS(),
    layout: state.getIn(['tiles', 'data', 'layout']).toJS(),
  };
};

export default connect(select)(MeView);
