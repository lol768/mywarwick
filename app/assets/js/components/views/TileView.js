import React, { Component, PropTypes } from 'react';
import * as TILE_TYPES from '../tiles';
import log from 'loglevel';
import Tile from '../tiles/Tile';
import { connect } from 'react-redux';
import { push, goBack } from 'react-router-redux';


class TileView extends Component {

  onTileExpand(tile) {
    this.props.dispatch(push(`/tiles/${tile.id}`));
  }

  onTileDismiss() {
    this.props.dispatch(goBack());
  }

  render() {
    const { id, zoomed, editing, editingAny, isDesktop, tile, content, size } = this.props;

    if (tile === undefined) {
      return <div />;
    }

    const view = this.props.view || this;

    const tileContentComponent = TILE_TYPES[tile.type];

    if (tileContentComponent === undefined) {
      log.error(`No component available for tile type ${tile.type}`);
      return null;
    }

    const tileProps = {
      ...tile,
      ...content,
      zoomed,
      size,
      canZoom: content ? tileContentComponent.canZoom(content.content) : false,
      key: id,
      id,
      editing,
      editingAny,
      isDesktop,
    };

    // Zooming
    tileProps.onZoomIn = () => this.onTileExpand(tileProps);
    tileProps.onZoomOut = () => this.onTileDismiss();

    // Editing
    tileProps.onBeginEditing = () => view.onBeginEditing(tileProps);
    tileProps.onHide = () => view.onHideTile(tileProps);
    tileProps.onResize = () => view.onResizeTile(tileProps);

    // subset of config needed by TileContent subclasses
    const contentProps = {
      ...content,
      zoomed,
      size,
      editingAny,
    };

    return (
      <Tile { ...tileProps }>
        { React.createElement(tileContentComponent, contentProps) }
      </Tile>
    );
  }

}

const select = (state, ownProps) => {
  const id = ownProps.id || ownProps.params.id;

  const tile = state.tiles.data.tiles.filter(t => t.id === id)[0];
  const content = state.tileContent[id];

  return {
    tile,
    content,
    isDesktop: state.ui.className === 'desktop',
    zoomed: ownProps.params !== undefined,
  };
};

TileView.propTypes = {
  id: PropTypes.string,
  params: PropTypes.object,
  dispatch: PropTypes.func,
  tile: PropTypes.object,
  content: PropTypes.object,
  isDesktop: PropTypes.bool,
  zoomed: PropTypes.bool,
  size: PropTypes.string,
  editingAny: PropTypes.bool,
  editing: PropTypes.bool,
  view: PropTypes.object,
};

export default connect(select)(TileView);
