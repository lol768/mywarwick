/* global document */
import React from 'react';
import * as PropTypes from 'prop-types';
import log from 'loglevel';
import _ from 'lodash-es';
import $ from 'jquery';
import { connect } from 'react-redux';
import { goBack, push } from 'react-router-redux';
import * as TILE_TYPES from '../tiles';
import Tile from '../tiles/Tile';
import { Routes } from '../AppRoot';
import ScrollRestore from '../ui/ScrollRestore';
import wrapKeyboardSelect from '../../keyboard-nav';

class TileView extends React.PureComponent {
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
      supportedTileSizes,
    };

    // Zooming
    tileProps.onZoomIn = () => this.onTileExpand(tileProps);
    tileProps.onZoomOut = () => this.onTileDismiss();

    // Editing
    tileProps.onHide = e => wrapKeyboardSelect(() => view.onHideTile(tileProps), e);
    tileProps.onResize = e => wrapKeyboardSelect(() => view.onResizeTile(tileProps), e);

    // subset of config needed by TileContent subclasses
    const contentProps = {
      ...content,
      zoomed,
      size,
      user,
      preferences: tile.preferences,
    };

    const tileElement = (
      <Tile { ...tileProps }>
        { React.createElement(tileContentComponent, contentProps) }
      </Tile>
    );

    if (zoomed) {
      return (
        <ScrollRestore url={`/${Routes.TILES}/${id}`} forceTop>
          {tileElement}
        </ScrollRestore>
      );
    }

    return tileElement;
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
  zoomed: PropTypes.bool,
  size: PropTypes.string.isRequired,
  editingAny: PropTypes.bool.isRequired,
  editing: PropTypes.bool.isRequired,
  view: PropTypes.object,
  user: PropTypes.object.isRequired,
};

TileView.defaultProps = {
  editing: false,
  editingAny: false,
  size: 'large',
};

export default connect(select)(TileView);
