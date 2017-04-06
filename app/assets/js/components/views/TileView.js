import React, { Component, PropTypes } from 'react';
import * as TILE_TYPES from '../tiles';
import log from 'loglevel';
import Tile from '../tiles/Tile';
import { connect } from 'react-redux';
import { push, goBack } from 'react-router-redux';
import _ from 'lodash-es';
import { Routes } from '../AppRoot';

class TileView extends Component {

  onTileExpand(tile) {
    this.props.dispatch(push(`/${Routes.TILES}/${tile.id}`));
  }

  onTileDismiss() {
    this.props.dispatch(goBack());
  }

  render() {
    const {
      id,
      zoomed,
      editing,
      editingAny,
      isDesktop,
      tile,
      content,
      size,
      option,
      user,
    } = this.props;

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
      option,
      user,
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

    // Configuring
    tileProps.onConfiguring = () => view.onConfiguring(tileProps);

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

  const tile = _.find(state.tiles.data.tiles, t => t.id === id);
  const content = state.tileContent[id];
  const option = state.tiles.data.options[id];
  const user = state.user.data;

  return {
    tile,
    content,
    option,
    user,
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
  option: PropTypes.object,
  isDesktop: PropTypes.bool,
  zoomed: PropTypes.bool,
  size: PropTypes.string,
  editingAny: PropTypes.bool,
  editing: PropTypes.bool,
  view: PropTypes.object,
  colour: React.PropTypes.number,
  user: PropTypes.object.isRequired,
};

export default connect(select)(TileView);
