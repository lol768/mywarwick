import React, { PropTypes } from 'react';
import ReactGridLayoutBase from 'react-grid-layout';
import _ from 'lodash-es';
import $ from 'jquery.transit';
import { connect } from 'react-redux';
import * as tiles from '../../state/tiles';
import HiddenTile from '../tiles/HiddenTile';
import ScrollRestore from '../ui/ScrollRestore';
import { Routes } from '../AppRoot';
import { isEmbedded } from '../../embedHelper';

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
    isDesktop: PropTypes.bool,
    deviceWidth: PropTypes.number,
    tiles: PropTypes.array,
  };

  constructor(props) {
    super(props);
    this.state = {};
  }

  onShowTile(tile) {
    this.props.dispatch(tiles.showTile(tile));
    this.props.dispatch(tiles.persistTiles());
  }

  getGridLayoutWidth() {
    const { isDesktop, deviceWidth } = this.props;

    const margins = _.sum(margin);
    if (isDesktop || isEmbedded()) {
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

const select = (state) => ({
  isDesktop: state.ui.className === 'desktop',
  layoutWidth: state.ui.isWideLayout === true ? 5 : 2,
  tiles: state.tiles.data.tiles,
  layout: state.tiles.data.layout,
  deviceWidth: state.device.width,
});

export default connect(select)(AddingTilesView);
