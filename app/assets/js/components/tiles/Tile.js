/* eslint react/sort-comp: 0 */
/* eslint-env browser */
import React from 'react';
import * as PropTypes from 'prop-types';
import _ from 'lodash-es';
import classNames from 'classnames';
import { localMoment } from '../../dateFormats';
import TileWrap from './TileWrap';
import { TILE_SIZES } from '../tiles/TileContent';

export default class Tile extends React.PureComponent {
  constructor(props) {
    super(props);
    this.state = {
      hasMounted: false,
    };

    this.onClick = this.onClick.bind(this);
    this.onClickExpand = this.onClickExpand.bind(this);
  }

  getIcon() {
    const { fetching, errors, icon } = this.props;

    const customIcon = this.getContentInstance() && this.getContentInstance().getIcon();

    const iconJsx = iconName => (
      <i
        className={`fa ${iconName} toggle-tooltip`}
        ref="icon"
        title={ this.getIconTitle() }
        data-toggle="tooltip"
        data-placement="auto"
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
    if (e.target === this.refs.icon || e.target === this.refs.zoom) {
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
    } else if (this.getContentInstance().expandsOnClick()) {
      this.props.onZoomIn(e);
    }
  }

  componentDidMount() {
    // Trigger an immediate re-render after the initial mount so we can access the content instance

    this.setState({ // eslint-disable-line react/no-did-mount-set-state
      hasMounted: true,
    });
  }

  getContentInstance() {
    return this.refs.tileWrap && this.refs.tileWrap.refs.content;
  }

  shouldDisplayExpandIcon() {
    return this.props.editing ? false : this.props.canZoom && !this.props.zoomed;
  }

  render() {
    const {
      type, title, size, colour, content, editing, zoomed, children, supportedTileSizes,
    } = this.props;

    const zoomIcon = () => {
      if (this.shouldDisplayExpandIcon()) {
        return <i ref="zoom" className="fa fa-expand" role="button" tabIndex={0} onClick={this.onClickExpand} />;
      }
      return null;
    };

    const tileSizeClasses = supportedTileSizes.map(s => `tile-size-supported--${s}`);

    const clickProps = (content && content.href) ? {
      onClick: this.onClick,
      role: 'button',
      tabIndex: 0,
    } : { onClick: this.onClick };

    return (
      <div className={`tile__container tile--${type}__container`}>
        <article
          { ...clickProps }
          className={
            classNames(
              'tile', `tile--${type}`, `tile--${size}`, `colour-${colour}`,
              {
                'tile--editing': editing,
                'tile--zoomed': zoomed,
              }, tileSizeClasses,
            )
          }
        >
          { this.getContentInstance() && this.getContentInstance().constructor.isRemovable() &&
            <div
              className="tile__edit-control top-left"
              onClick={ this.props.onHide }
              role="button"
              tabIndex={0}
              title={ `Hide ${title}` }
            >
              <div className="icon"><i className="fa fa-minus" /></div>
            </div>
          }

          <div
            className="tile__edit-control bottom-right"
            onClick={ this.props.onResize }
            role="button"
            tabIndex={0}
            title={`Make tile ${_.last(supportedTileSizes) === size ? 'smaller' : 'bigger'}`}
          >
            <div className="icon"><i className="fa fa-arrow-up" /></div>
          </div>

          <div
            className="tile__edit-control bottom-left tile__drag-handle"
            title="Drag to re-arrange tile"
          >
            <div className="icon"><i className="fa fa-arrows" /></div>
          </div>

          <TileWrap
            icon={this.getIcon()}
            zoomIcon={zoomIcon()}
            title={title}
            children={children}
            onClickExpand={this.onClickExpand}
            ref="tileWrap"
          />
        </article>
      </div>
    );
  }

  static propTypes = {
    children: PropTypes.node,
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
    supportedTileSizes: PropTypes.arrayOf(t => _.values(TILE_SIZES).indexOf(t) !== -1).isRequired,
  }
}
