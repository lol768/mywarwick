import React, { PropTypes } from 'react';
import ReactGridLayoutBase from 'react-grid-layout';
import ReactCSSTransitionGroup from 'react/lib/ReactCSSTransitionGroup';
import _ from 'lodash-es';
import $ from 'jquery.transit';
import { connect } from 'react-redux';
import classNames from 'classnames';
import { goBack, replace, push } from 'react-router-redux';
import * as tiles from '../../state/tiles';
import { TILE_SIZES } from '../tiles/TileContent';
import TileView from './TileView';
import * as TILE_TYPES from '../tiles';
import TileOptionView from './TileOptionView';
import { Routes } from '../AppRoot';
import ScrollRestore from '../ui/ScrollRestore';

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

class MeView extends React.Component {

  static propTypes = {
    location: PropTypes.shape({
      pathname: PropTypes.string,
    }),
    navRequest: PropTypes.string,
    dispatch: PropTypes.func.isRequired,
    editing: PropTypes.bool,
    adding: PropTypes.bool,
    layoutWidth: PropTypes.number,
    layout: PropTypes.array,
    isDesktop: PropTypes.bool,
    deviceWidth: PropTypes.number,
    tiles: PropTypes.array,
  };

  constructor(props) {
    super(props);
    this.state = {};
    this.onTileDismiss = this.onTileDismiss.bind(this);
    this.onLayoutChange = this.onLayoutChange.bind(this);
    this.onDragStart = this.onDragStart.bind(this);
    this.onConfigSave = this.onConfigSave.bind(this);
    this.onConfigViewDismiss = this.onConfigViewDismiss.bind(this);
    this.onAdd = this.onAdd.bind(this);
  }

  componentWillReceiveProps(nextProps) {
    if (this.props.editing && !nextProps.editing) {
      this.onFinishEditing();
    }
  }

  /**
   * The other half of this is in ui.navRequest.
   * If we've been sent back only to go forward, get the requested path from the store, reset
   * that state, and replace the current path.
   */
  componentDidUpdate() {
    if (this.props.navRequest) {
      if (this.props.location.pathname === '/') {
        const path = this.props.navRequest;
        this.props.dispatch({
          type: 'ui.navRequest',
          navRequest: null,
        });
        this.props.dispatch(replace(path));
      } else {
        this.props.dispatch(goBack());
      }
    }
  }

  onFinishEditing() {
    this.props.dispatch(tiles.persistTiles());
  }

  onDragStart(layout, item, newItem, placeholder, e) {
    e.preventDefault();

    // Disable rubber banding so the users' finger and the tile they are dragging
    // don't get out of sync.  (iOS)
    $('.id7-main-content-area').css('-webkit-overflow-scrolling', 'auto');
  }

  onLayoutChange(layout) {
    if (this.previousLayout === undefined || !_.isEqual(layout, this.previousLayout)) {
      this.props.dispatch(tiles.tileLayoutChange(layout, this.props.layoutWidth));
      this.previousLayout = _.cloneDeep(layout);
    }
  }

  onHideTile(tile) {
    // Block transitions until we really want them
    $('.me-view-container .me-view').addClass('with-transitions');

    this.props.dispatch(tiles.hideTile(tile));

    this.onFinishEditing();
  }

  onResizeTile(tileProps) {
    // Block transitions until we really want them
    $('.me-view-container .me-view').addClass('with-transitions');

    const sizes = tileProps.supportedTileSizes;
    const nextSizeIndex = sizes.indexOf(tileProps.size || tileProps.defaultSize) + 1;
    const nextSize = sizes[nextSizeIndex % sizes.length];

    const { width, height } = getSizeFromSizeName(nextSize);

    this.props.dispatch(tiles.resizeTile(tileProps, this.props.layoutWidth, width, height));
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

  onTileDismiss() {
    this.props.dispatch(goBack());
  }

  onAdd() {
    this.props.dispatch(push(`/${Routes.EDIT}/${Routes.ADD}`));
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

  getGridLayoutWidth() {
    const { isDesktop, deviceWidth } = this.props;

    const margins = _.sum(margin);

    if (isDesktop) {
      return $('.id7-main-content').width() + margins;
    }

    return deviceWidth + margins;
  }

  renderTile(props) {
    const { id } = props;

    return (
      <TileView
        ref={id}
        key={id}
        id={id}
        view={this}
        editing={this.props.editing}
        editingAny={this.props.editing}
        size={this.getTileSize(id)}
        layoutWidth={this.props.layoutWidth}
      />
    );
  }

  renderTiles() {
    const { layoutWidth, isDesktop, editing } = this.props;
    const visibleTiles = this.props.tiles.filter(t => !t.removed
    && (TILE_TYPES[t.type].isVisibleOnDesktopOnly() ? isDesktop : true));
    const hiddenTiles = this.props.tiles.filter(t => t.removed
    && (TILE_TYPES[t.type].isVisibleOnDesktopOnly() ? isDesktop : true));

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
            onLayoutChange={this.onLayoutChange}
            verticalCompact
            onDragStart={this.onDragStart}
            width={this.getGridLayoutWidth()}
            draggableHandle=".tile__drag-handle"
          >
            { tileComponents }
          </ReactGridLayoutBase>
          <ReactCSSTransitionGroup
            transitionName="grow-shrink"
            transitionAppear
            transitionAppearTimeout={500}
            transitionEnterTimeout={500}
            transitionLeaveTimeout={300}
          >{ editing && hiddenTiles.length > 0 ?
            <div key="add-tile-button" className="add-tile-button" onClick={this.onAdd}>
              <i className="fa fa-plus" />
            </div>
            : null }
          </ReactCSSTransitionGroup>
        </div>
      </div>
    );
  }

  renderTileOptionsView() {
    if (this.state.configuringTile && this.props.editing) {
      const configuringTile = this.state.configuringTile;
      return (
        <div>
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
    const classes = classNames('me-view', { 'me-view--editing': this.props.editing });

    return (
      <ScrollRestore url="/">
        <div className="me-view-container">
          <div className={classes}>
            {this.renderTiles()}
            {this.renderTileOptionsView()}
          </div>
        </div>
      </ScrollRestore>
    );
  }
}

const select = (state) => ({
  isDesktop: state.ui.className === 'desktop',
  layoutWidth: state.ui.isWideLayout === true ? 5 : 2,
  tiles: state.tiles.data.tiles,
  layout: state.tiles.data.layout,
  deviceWidth: state.device.width,
  navRequest: state.ui.navRequest,
});

export default connect(select)(MeView);
