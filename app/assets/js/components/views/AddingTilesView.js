import React from 'react';
import * as PropTypes from 'prop-types';
import ReactGridLayoutBase from 'react-grid-layout';
import _ from 'lodash-es';
import { connect } from 'react-redux';
import * as tiles from '../../state/tiles';
import HiddenTile from '../tiles/HiddenTile';
import ScrollRestore from '../ui/ScrollRestore';
import { Routes } from '../AppRoot';
import GridSizingHelper from '../../GridSizingHelper';

const rowHeight = 125;
const margin = [4, 4];

class AddingTilesView extends React.PureComponent {
  static propTypes = {
    location: PropTypes.shape({
      pathname: PropTypes.string,
    }),
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

    this.onShowTile = this.onShowTile.bind(this);
    this.state = {};
  }

  onShowTile(tile) {
    this.props.dispatch(tiles.showTile(tile));
    this.props.dispatch(tiles.fetchTileContent(tile.id));
    this.props.dispatch(tiles.persistTiles());
  }

  getGridLayoutWidth() {
    return GridSizingHelper.getGridLayoutWidth(margin);
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
      (<div key={ tile.id }>
        <HiddenTile {...tile} onShow={ this.onShowTile } />
      </div>),
    );

    return (
      <div>
        <h3>Add more tiles</h3>
        { hiddenTileComponents.length > 0 ?
          <ReactGridLayoutBase
            layout={layout}
            isDraggable={false}
            isResizable={false}
            cols={layoutWidth}
            rowHeight={rowHeight}
            margin={margin}
            verticalCompact
            width={this.getGridLayoutWidth()}
          >
            { hiddenTileComponents }
          </ReactGridLayoutBase>
          : <p>There are no more tiles available to add</p>
        }
      </div>
    );
  }

  render() {
    return (
      <ScrollRestore
        url={`/${Routes.EDIT}/${Routes.ADD}`}
        forceTop
      >
        <div className="me-view-container">
          <div className="me-view me-view--adding">
            {this.renderHiddenTiles()}
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

export default connect(select)(AddingTilesView);
