import React from 'react';
import * as PropTypes from 'prop-types';

/**
 * Header and body of the tile. Separate component so that we can
 * toggle edit buttons without this one needing to trigger an update.
 *
 * (Still triggers an update, probably because the icons are always new objects.)
 */
export default class TileWrap extends React.PureComponent {
  static propTypes = {
    children: PropTypes.node.isRequired,
    icon: PropTypes.node.isRequired,
    zoomIcon: PropTypes.node,
    title: PropTypes.string.isRequired,
    onClickExpand: PropTypes.func.isRequired,
  };

  render() {
    const { icon, zoomIcon, title, children, onClickExpand } = this.props;
    return (
      <div className="tile__wrap">
        <header className="tile__header">
          <div className="tile__icon tile__icon--left">{icon}</div>
          <div className="tile__icon tile__icon--right">{zoomIcon}</div>
          <div className="tile__title">{title}</div>
        </header>
        <div className="tile__body" data-scrollable>
          { React.cloneElement(
            React.Children.only(children),
            {
              ref: 'content',
              onClickExpand,
            },
          )}
        </div>
      </div>
    );
  }
}

