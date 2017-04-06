/* eslint react/sort-comp: 0 */
import React, { PropTypes } from 'react';
import _ from 'lodash-es';

import { localMoment } from '../../dateFormats.js';
import classNames from 'classnames';

import { TILE_SIZES } from '../tiles/TileContent';

export default class Tile extends React.Component {

  constructor(props) {
    super(props);
    this.state = {
      contentRef: null,
    };

    this.onClick = this.onClick.bind(this);
    this.onClickExpand = this.onClickExpand.bind(this);
  }

  getIcon() {
    const { fetching, errors, icon, content } = this.props;

    // FIXME: shouldn't have to pass content here, the TileContent component has its own content
    const customIcon = (content && this.state.contentRef) ?
      this.state.contentRef.getIcon(content)
      : null;

    const iconJsx = iconName => (
      <i className={`fa ${iconName} toggle-tooltip`} ref="icon" title={ this.getIconTitle() }
        data-toggle="tooltip" data-placement="auto"
      />);

    if (fetching) {
      return iconJsx('fa-refresh fa-spin');
    } else if (errors) {
      return iconJsx('fa-exclamation-triangle');
    } else if (customIcon) {
      return customIcon;
    } else if (icon) {
      return iconJsx(`fa-${icon}`);
    }
    return iconJsx('fa-question-circle');
  }

  getIconTitle() {
    const { errors, fetchedAt } = this.props;

    if (errors) {
      return `Last updated ${localMoment(fetchedAt).calendar()}. ${errors[0].message}`;
    }

    return null;
  }

  onClickExpand(e) {
    e.preventDefault();
    this.props.onZoomIn(e);
  }

  onClick(e) {
    if (e.target === this.refs.icon) {
      // Do not apply default click action to icon
      return;
    }

    const { content, editingAny } = this.props;
    e.stopPropagation();
    if (editingAny) {
      e.preventDefault();
    } else if (content && content.href) {
      if (window.navigator.userAgent.indexOf('MyWarwick/') >= 0) {
        window.location = content.href;
      } else {
        window.open(content.href);
      }
    }
  }

  componentDidMount() {
    this.setState({ // eslint-disable-line react/no-did-mount-set-state
      contentRef: this.refs.content,
    });
  }

  shouldDisplayExpandIcon() {
    return this.props.editing ? false : this.props.canZoom;
  }

  displayConfigButton() {
    const hasOption = !_.isEmpty(this.props.option);
    const userLoggedIn = this.props.user ? this.props.user.authenticated : false;
    if (hasOption && userLoggedIn) {
      return (
        <div
          className="tile__edit-control top-right"
          title="Change setting"
          onClick={this.props.onConfiguring}
        >
          <i className="fa fa-fw fa-pencil"></i>
        </div>
      );
    }
    return null;
  }

  render() {
    const { type, title, size, colour, content, editing, zoomed, isDesktop, children } = this.props;

    const zoomIcon = () => {
      if (zoomed) {
        return isDesktop ?
          <i className="fa fa-times" onClick={this.props.onZoomOut}> </i> : null;
      } else if (this.shouldDisplayExpandIcon()) {
        return <i className="fa fa-expand" onClick={this.onClickExpand}> </i>;
      }
      return null;
    };

    return (
      <div className="tile__container">
        <article
          className={
            classNames(
              'tile', `tile--${type}`, `tile--${size}`, `colour-${colour}`,
              {
                'tile--editing': editing,
                'tile--zoomed': zoomed,
                'cursor-pointer': content && content.href,
              }
            )
          }
          onClick={ this.onClick }
          ref="tile"
        >
          { this.state.contentRef && this.state.contentRef.isRemovable() &&
            <div
              className="tile__edit-control top-left"
              onClick={ this.props.onHide }
              title={ `Hide ${title}` }
            >
              <i className="fa fa-fw fa-minus"> </i>
            </div>
          }

          <div
            className="tile__edit-control bottom-right"
            onClick={ this.props.onResize }
            title={`Make tile ${size !== 'tall' ? 'bigger' : 'smaller'}`}
          >
            <i className="fa fa-fw fa-arrow-up"> </i>
          </div>

          <div
            className="tile__edit-control bottom-left tile__drag-handle"
            title="Drag to re-arrange tile"
          >
            <i className="fa fa-fw fa-arrows"> </i>
          </div>

          { this.displayConfigButton() }

          <TileWrap
            icon={this.getIcon()}
            zoomIcon={zoomIcon()}
            title={title}
            children={children}
            onClickExpand={this.onClickExpand}
          />
        </article>
      </div>
    );
  }

  static propTypes = {
    children: PropTypes.node,
    onConfiguring: PropTypes.func,
    onResize: PropTypes.func.isRequired,
    onHide: PropTypes.func.isRequired,
    onZoomOut: PropTypes.func.isRequired,
    onZoomIn: PropTypes.func.isRequired,
    type: PropTypes.string.isRequired,
    title: PropTypes.string.isRequired,
    icon: PropTypes.string.isRequired,
    size: PropTypes.oneOf(_.values(TILE_SIZES)),
    editing: PropTypes.bool.isRequired,
    editingAny: PropTypes.bool.isRequired,
    zoomed: PropTypes.bool.isRequired,
    isDesktop: PropTypes.bool.isRequired,
    option: PropTypes.object,
    id: PropTypes.string,
    canZoom: PropTypes.bool.isRequired,
    fetching: PropTypes.bool,
    errors: PropTypes.arrayOf(PropTypes.shape({
      message: PropTypes.string,
    })),
    content: PropTypes.shape({
      href: PropTypes.string,
    }),
    colour: PropTypes.number.isRequired,
    fetchedAt: PropTypes.number,
    user: PropTypes.object,
  }
}

/**
 * Header and body of the tile. Separate component so that we can
 * toggle edit buttons without this one needing to trigger an update.
 *
 * (Still triggers an update, probably because the icons are always new objects.)
 */
class TileWrap extends React.PureComponent {
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
            }
          )}
        </div>
      </div>
    );
  }

  static propTypes = {
    children: PropTypes.node.isRequired,
    icon: PropTypes.node.isRequired,
    zoomIcon: PropTypes.node,
    title: PropTypes.string.isRequired,
    onClickExpand: PropTypes.func.isRequired,
  }
}

