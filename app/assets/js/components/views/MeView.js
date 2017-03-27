import React from 'react';
import ReactDOM from 'react-dom';
import ReactComponent from 'react/lib/ReactComponent';
import ReactCSSTransitionGroup from 'react/lib/ReactCSSTransitionGroup';
import ReactGridLayoutBase from 'react-grid-layout';
import _ from 'lodash';
import $ from 'jquery.transit';
import { connect } from 'react-redux';
import classNames from 'classnames';
import { goBack } from 'react-router-redux';
import * as tiles from '../../state/tiles';
import { TILE_SIZES } from '../tiles/TileContent';
import TileView from './TileView';
import * as TILE_TYPES from '../tiles';
import TileOptionView from './TileOptionView';

import HiddenTile from '../tiles/HiddenTile';

const rowHeight = 125;
const margin = [4, 4];

function getSizeFromSizeName(name) {
  const sizes = {
    [TILE_SIZES.SMALL]: { width: 1, height: 1 },
    [TILE_SIZES.WIDE]: { width: 2, height: 1 },
    [TILE_SIZES.LARGE]: { width: 2, height: 2 },
    [TILE_SIZES.TALL]: { width: 2, height: 4 },
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

  if (width === 2 && height === 4) {
    return TILE_SIZES.TALL;
  }

  return TILE_SIZES.LARGE;
}

class MeView extends ReactComponent {

  constructor(props) {
    super(props);
    this.state = {};
    this.onClickOutside = this.onClickOutside.bind(this);
    this.onTileDismiss = this.onTileDismiss.bind(this);
    this.onLayoutChange = this.onLayoutChange.bind(this);
    this.onDragStart = this.onDragStart.bind(this);
    this.onDragStop = this.onDragStop.bind(this);
    this.getDragDelayForItem = this.getDragDelayForItem.bind(this);
    this.onConfigSave = this.onConfigSave.bind(this);
    this.onConfigViewDismiss = this.onConfigViewDismiss.bind(this);
  }

  onBeginEditing(tile) {
    this.setState({
      editing: tile.id,
    });

    $(ReactDOM.findDOMNode(this)).on('click', this.onClickOutside);
  }

  onFinishEditing() {
    if (!this.unmounted) {
      this.setState({
        editing: null,
      });
    }

    $(ReactDOM.findDOMNode(this)).off('click', this.onClickOutside);

    this.props.dispatch(tiles.persistTiles());
  }

  onClickOutside(e) {
    if (this.state.configuringTile) {
      return;
    }

    if (this.state.editing && $(e.target).parents('.tile--editing').length === 0) {
      // Defer so this click is still considered to be happening in editing mode
      _.defer(() => {
        this.onFinishEditing();
        this.onConfigViewDismiss();
      });
    }
  }

  onDragStart(layout, item) {
    this.onBeginEditing({ id: item.i });
  }

  onDragStop() {
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
        layoutWidth={this.props.layoutWidth}
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
    this.props.dispatch(tiles.persistTiles());
    this.props.dispatch(tiles.fetchTileContent(tile.id));

    this.onFinishEditing();
  }

  onConfiguring(tileProps) {
    this.setState({
      configuringTile: tileProps,
    });
  }

  onConfigViewDismiss() {
    this.setState({
      configuringTile: null,
    });
  }

  onConfigSave(tile, preferences) {
    this.props.dispatch(tiles.saveTilePreferences(tile, preferences));
    this.onConfigViewDismiss();
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

  getDragDelayForItem(item) {
    return this.state.editing === item.i ? 0 : 400;
  }

  getGridLayoutWidth() {
    const { isDesktop, deviceWidth } = this.props;

    const margins = _.sum(margin);

    if (isDesktop) {
      return $('.id7-main-content').width() + margins;
    }

    return deviceWidth + margins;
  }

  renderHiddenTiles() {
    const { layoutWidth } = this.props;
    const hiddenTiles = _.sortBy(this.props.tiles.filter(t => t.removed), 'title');

    const layout = hiddenTiles
      .map((item, i) => ({
        i: item.id,
        x: i % layoutWidth,
        y: Math.floor(i / layoutWidth),
        w: 1,
        h: 1,
      }));

    const hiddenTileComponents = hiddenTiles.map(tile =>
      <div key={ tile.id }>
        <HiddenTile {...tile} onShow={ () => this.onShowTile(tile) } />
      </div>
    );

    return (
      <div>
        <h3>More tiles</h3>
        <ReactGridLayoutBase
          layout={layout}
          isDraggable={false}
          isResizable={false}
          cols={layoutWidth}
          rowHeight={rowHeight}
          margin={margin}
          useCSSTransformations
          verticalCompact
          width={this.getGridLayoutWidth()}
        >
          { hiddenTileComponents }
        </ReactGridLayoutBase>
      </div>
    );
  }

  renderTiles() {
    const { layoutWidth, isDesktop } = this.props;
    const visibleTiles = this.props.tiles.filter(t => !t.removed
    && (TILE_TYPES[t.type].isVisibleOnDesktopOnly() ? isDesktop : true));
    const hiddenTiles = this.props.tiles.filter(t => t.removed
    && (TILE_TYPES[t.type].isVisibleOnDesktopOnly() ? isDesktop : true));
    const { editing } = this.state;

    // Show hidden tiles (if any) when editing, or if there are no visible tiles
    const showHiddenTiles = hiddenTiles.length > 0 && (editing || visibleTiles.length === 0);

    const layout = this.getTileLayout(this.props.layout, layoutWidth);
    const tileComponents = visibleTiles.map(tile =>
      <div
        key={tile.id}
        className={editing === tile.id ? 'react-grid-item--editing' : ''}
        style={{ touchAction: 'auto' }} // Allow touches to scroll (overrides react-draggable)
      >
        { this.renderTile(tile) }
      </div>
    );

    return (
      <div>
        <div className="me-view__tiles">
          <ReactGridLayoutBase
            layout={layout}
            isDraggable
            isResizable={false}
            cols={layoutWidth}
            rowHeight={rowHeight}
            margin={margin}
            useCSSTransformations
            onLayoutChange={this.onLayoutChange}
            verticalCompact
            draggableCancel=".tile__edit-control, .toggle-tooltip"
            onDragStart={this.onDragStart}
            onDragStop={this.onDragStop}
            getDragDelayForItem={this.getDragDelayForItem}
            width={this.getGridLayoutWidth()}
          >
            { tileComponents }
          </ReactGridLayoutBase>
        </div>
        { showHiddenTiles ? this.renderHiddenTiles() : null }
      </div>
    );
  }

  onTileDismiss() {
    this.props.dispatch(goBack());
  }

  renderTileOptionsView() {
    if (this.state.configuringTile && this.state.editing) {
      const configuringTile = this.state.configuringTile;
      return (
        <div>
          <div className="tile-zoom-backdrop" onClick={this.onConfigViewDismiss}></div>
          <TileOptionView
            tile={ configuringTile }
            onConfigViewDismiss= { this.onConfigViewDismiss }
            onConfigSave = { this.onConfigSave }
          />
        </div>
      );
    }
    return null;
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
        {this.renderTileOptionsView()}
        <ReactCSSTransitionGroup {...transitionProps}>
          { this.props.children }
        </ReactCSSTransitionGroup>
      </div>
    );
  }
}

const select = (state) => ({
  isDesktop: state.ui.className === 'desktop',
  layoutWidth: state.ui.isWideLayout === true ? 5 : 2,
  tiles: state.tiles.data.tiles,
  layout: state.tiles.data.layout,
  deviceWidth: state.device.width,
});

export default connect(select)(MeView);
