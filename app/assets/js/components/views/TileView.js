import React, { Component, PropTypes } from 'react';
import * as TILE_TYPES from '../tiles';
import log from 'loglevel';
import Tile from '../tiles/Tile';
import { connect } from 'react-redux';
import { goBack, push } from 'react-router-redux';
import _ from 'lodash-es';
import $ from 'jquery';
import { Routes } from '../AppRoot';
import ScrollRestore from '../ui/ScrollRestore';

class TileView extends Component {

  componentDidMount() {
    if (this.props.tile && this.props.zoomed) {
      $(document.body).addClass(`colour-${this.props.tile.colour}`);
    }
  }

  componentWillReceiveProps(nextProps) {
    if (this.props.zoomed) {
      if (this.props.tile) {
        $(document.body).removeClass(`colour-${this.props.tile.colour}`);
      }

      if (nextProps.tile) {
        $(document.body).addClass(`colour-${nextProps.tile.colour}`);
      }
    }
  }

  componentWillUnmount() {
    if (this.props.tile && this.props.zoomed) {
      $(document.body).removeClass(`colour-${this.props.tile.colour}`);
    }
  }

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

    let canZoom = false;
    let supportedTileSizes = [];
    if (content && tileContentComponent.needsContentToRender()) {
      canZoom = tileContentComponent.canZoom(content.content);
      supportedTileSizes = tileContentComponent.supportedTileSizes(content.content);
    } else {
      canZoom = tileContentComponent.canZoom();
      supportedTileSizes = tileContentComponent.supportedTileSizes();
    }

    const tileProps = {
      ...tile,
      ...content,
      option,
      user,
      zoomed,
      size,
      canZoom,
      key: id,
      id,
      editing,
      editingAny,
      isDesktop,
      supportedTileSizes,
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
    };

    return (
      <ScrollRestore url={`/${Routes.TILES}/${id}`} forceTop hiddenView={ this.props.hiddenView }>
        <Tile { ...tileProps }>
          { React.createElement(tileContentComponent, contentProps) }
        </Tile>
      </ScrollRestore>
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
  size: PropTypes.string.isRequired,
  editingAny: PropTypes.bool.isRequired,
  editing: PropTypes.bool.isRequired,
  view: PropTypes.object,
  user: PropTypes.object.isRequired,
  hiddenView: PropTypes.bool.isRequired,
};

TileView.defaultProps = {
  editing: false,
  editingAny: false,
  size: 'large',
};

export default connect(select)(TileView);
