import React from 'react';
import * as PropTypes from 'prop-types';
import ReactGridLayoutBase from 'react-grid-layout';
import _ from 'lodash-es';
import $ from 'jquery';
import { connect } from 'react-redux';
import classNames from 'classnames';
import ReactCSSTransitionGroup from 'react/lib/ReactCSSTransitionGroup';
import { goBack, push } from 'react-router-redux';
import * as tiles from '../../state/tiles';
import { TILE_SIZES } from '../tiles/TileContent';
import TileView from './TileView';
import * as TILE_TYPES from '../tiles';
import ScrollRestore from '../ui/ScrollRestore';
import GridSizingHelper from '../../GridSizingHelper';
import { Routes } from '../AppRoot';
import wrapKeyboardSelect from '../../keyboard-nav';
import { pluralise } from '../../helpers';

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

class MeView extends React.PureComponent {
  static propTypes = {
    dispatch: PropTypes.func.isRequired,
    editing: PropTypes.bool,
    adding: PropTypes.bool,
    layoutWidth: PropTypes.number,
    layout: PropTypes.array,
    deviceWidth: PropTypes.number,
    tiles: PropTypes.array,
  };

  constructor(props) {
    super(props);
    this.onTileDismiss = this.onTileDismiss.bind(this);
    this.onLayoutChange = this.onLayoutChange.bind(this);
    this.onDragStart = this.onDragStart.bind(this);
    this.onAdd = this.onAdd.bind(this);
  }

  onDragStart(layout, item, newItem, placeholder, e) {
    e.preventDefault();

    // Disable rubber banding so the users' finger and the tile they are dragging
    // don't get out of sync.  (iOS)
    $('.id7-main-content-area').css('-webkit-overflow-scrolling', 'auto');
  }

  onLayoutChange(layout) {
    if (this.props.tiles.length === 0) {
      // If the store has been cleared don't save the new layout
      return false;
    } else if (this.previousLayout === undefined) {
      this.previousLayout = _.cloneDeep(layout);
    } else if (!_.isEqual(layout, this.previousLayout)) {
      this.props.dispatch(tiles.tileLayoutChange(layout, this.props.layoutWidth));
      this.previousLayout = _.cloneDeep(layout);
      this.props.dispatch(tiles.persistTiles());
    }
    return true;
  }

  onHideTile(tile) {
    // Block transitions until we really want them
    $('.me-view-container .me-view').addClass('with-transitions');

    this.props.dispatch(tiles.hideTile(tile));
    // Persistence done by layout change
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

  onTileDismiss() {
    this.props.dispatch(goBack());
  }

  onAdd(e) {
    wrapKeyboardSelect(() => this.props.dispatch(push(`/${Routes.EDIT}/${Routes.ADD}`)), e);
  }

  getTileSize(id) {
    const layout = this.props.layout.filter(i =>
      i.tile === id && i.layoutWidth === this.props.layoutWidth,
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
    return GridSizingHelper.getGridLayoutWidth(margin);
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
    const { layoutWidth, editing } = this.props;

    const allTiles = this.props.tiles.filter(t => TILE_TYPES[t.type]);

    const visibleTiles = allTiles.filter(t => !t.removed);
    const hiddenTiles = allTiles.filter(t => t.removed);

    const layout = this.getTileLayout(this.props.layout, layoutWidth);
    const tileComponents = visibleTiles.map(tile =>
      (<div
        key={tile.id}
        className={editing === tile.id ? 'react-grid-item--editing' : ''}
        style={{ touchAction: 'auto' }} // Allow touches to scroll (overrides react-draggable)
      >
        { this.renderTile(tile) }
      </div>),
    );

    /* eslint-disable */
    // Justification: conflicting rules here result in a wild goose chase
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
          <div className="add-tile-container">
            <ReactCSSTransitionGroup
              transitionName="grow-shrink"
              transitionAppear
              transitionAppearTimeout={500}
              transitionEnterTimeout={500}
              transitionLeaveTimeout={300}
            >{ editing && hiddenTiles.length > 0 ?
              <div
                key="add-tile-button"
                className="add-tile-button"
                onClick={this.onAdd}
                onKeyUp={this.onAdd}
                role="button"
                tabIndex={0}
              >
                <i className="fa fa-plus"/>
                <div className="add-tile-button__text">
                  <div className="num-hidden-tiles">{hiddenTiles.length}</div>
                  <div>
                    hidden<br/>{pluralise('tile', hiddenTiles.length)}
                  </div>
                </div>
              </div>
              : null}
            </ReactCSSTransitionGroup>
          </div>
        </div>
      </div>
    );
    /* eslint-enable */
  }

  render() {
    const classes = classNames('me-view', { 'me-view--editing': this.props.editing });

    return (
      <ScrollRestore url="/">
        <div className="me-view-container">
          <div className={classes}>
            {this.renderTiles()}
          </div>
        </div>
      </ScrollRestore>
    );
  }
}

const select = state => ({
  layoutWidth: state.ui.layoutWidth,
  tiles: state.tiles.data.tiles,
  layout: state.tiles.data.layout,
  deviceWidth: state.device.width,
});

export default connect(select)(MeView);
