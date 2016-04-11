import React from 'react';
import ReactDOM from 'react-dom';
import ReactComponent from 'react/lib/ReactComponent';
import ReactCSSTransitionGroup from 'react/lib/ReactCSSTransitionGroup';

const widthProvider = require('react-grid-layout').WidthProvider;
let ReactGridLayout = require('react-grid-layout');
ReactGridLayout = widthProvider(ReactGridLayout);

import _ from 'lodash';
import $ from 'jquery.transit';
import { connect } from 'react-redux';
import classNames from 'classnames';
import { goBack } from 'react-router-redux';

import * as tiles from '../../tiles';
import * as serverpipe from '../../serverpipe';
import { TILE_SIZES } from '../tiles/TileContent';
import TileView from './TileView';

import { EDITING_ANIMATION_DURATION } from '../tiles/Tile';

import HiddenTile from '../tiles/HiddenTile';

class MeView extends ReactComponent {

  constructor(props) {
    super(props);
    this.state = {
      layout: [],
    };
    this.onBodyClick = this.onBodyClick.bind(this);
    this.onTileDismiss = this.onTileDismiss.bind(this);
    this.onLayoutChange = this.onLayoutChange.bind(this);
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

  // FIXME: dodgy af. Try to find more reliable way of doing tileOrder => layout
  calcTileLayout(tiles, isDesktop) {
    function position(tile) {
      if (isDesktop) {
        return tile.positionDesktop;
      }

      return tile.positionMobile;
    }

    return tiles.filter(tile => !tile.removed).map(tile => {

      let y = Math.floor(position(tile) / 10);
      let x = position(tile) % 10;

      // FIXME code of a madman
      if (!this.props.isDesktop) {
        y *= 2;
        if (x >= 2) {
          x -= 2;
          y += 1;
        }
      }

      const layout = {
        i: tile.id,
        y,
        x,
        w: tile.size === 'small' ? 1 : 2,
        h: (tile.size === 'large' ? 2 : 1),
      };

      return layout;
    });
  }

  componentWillReceiveProps(newProps) {
    if (!this.didTriggerTileLayoutChange) {
      // fine to re-render stuff
      this.setState({
        layout: this.calcTileLayout(newProps.tiles, newProps.isDesktop),
      });
    } else {
      // don't re-render stuff because it will infinite loop
    }
  }

  onLayoutChange(layout) {
    this.didTriggerTileLayoutChange = true;
    this.props.dispatch(tiles.tileLayoutChange(layout, this.props.isDesktop));
    delete this.didTriggerTileLayoutChange;
  }

  renderTile(props) {
    return (
      <TileView
        key={props.id}
        id={props.id}
        view={this}
        editing={this.state.editing === props.id}
        editingAny={!!this.state.editing}
      />
    );
  }

  onHideTile(tile) {
    this.props.dispatch(tiles.hideTile(tile));

    this.onFinishEditing();
  }

  // FIXME: this should fire action to update tile w/h in layout
  onResizeTile(tile) {
    const sizes = _.values(TILE_SIZES);
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
        <ReactGridLayout layout={this.state.layout}
                         isDraggable={!!editing}
                         isResizable={false}
                         cols={this.props.isDesktop ? 4 : 2}
                         rowHeight={125}
                         margin={[4, 4]}
                         useCSSTransformations={true}
                         onDragStop={this.onLayoutChange}
                         verticalCompact={true}
        >
          {tileComponents.map(component => <div key={component.props.id}>{component}</div>)}
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
    const classes = classNames('row', 'me-view', { 'me-view--editing': this.state.editing });
    const isDesktop = this.props.isDesktop;
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
  const items = state.getIn(['tiles', 'items']);

  return {
    isDesktop: state.getIn(['ui', 'className']) === 'desktop',
    tiles: items.filterNot(tile => tile.get('removed')).toJS(),
    hiddenTiles: items.filter(tile => tile.get('removed')).toJS(),
  };
};

export default connect(select)(MeView);
