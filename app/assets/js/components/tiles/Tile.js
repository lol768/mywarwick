/* eslint react/sort-comp: 0 */
/* eslint-env browser */
import React from 'react';
import * as PropTypes from 'prop-types';
import _ from 'lodash-es';
import classNames from 'classnames';
import { localMoment } from '../../dateFormats';
import TileWrap from './TileWrap';
import { TILE_SIZES } from '../tiles/TileContent';
import wrapKeyboardSelect from '../../keyboard-nav';
import $ from 'jquery';
import HideableView from '../views/HideableView';

export default class Tile extends HideableView {
  constructor(props) {
    super(props);
    this.state = {
      hasMounted: false,
      zooming: false,
    };

    this.onClick = this.onClick.bind(this);
    this.onClickExpand = this.onClickExpand.bind(this);
  }

  getIcon() {
    const { fetching, errors, icon } = this.props;

    const customIcon = this.getContentInstance() && this.getContentInstance().getIcon();

    const iconJsx = (iconStyle, iconName) => (
      <i
        className={`${iconStyle} ${iconName} toggle-tooltip`}
        ref="icon"
        title={ this.getIconTitle() }
        data-toggle="tooltip"
        data-placement="auto"
      />);

    if (fetching) {
      return iconJsx('fal', 'fa-sync fa-spin');
    } else if (errors) {
      return iconJsx('fal', 'fa-exclamation-triangle');
    } else if (customIcon) {
      return customIcon;
    } else if (icon === 'bus' || icon === 'car') {
      return iconJsx('fas', `fa-${icon}`);
    } else if (icon) {
      return iconJsx('fal', `fa-${icon}`);
    }
    return iconJsx('fal', 'fa-question-circle');
  }

  getIconTitle() {
    const { errors, fetchedAt } = this.props;

    if (errors && errors.length) {
      return `Last updated ${localMoment(fetchedAt).calendar()}. ${errors[0].message}`;
    }

    return null;
  }

  onClickExpand(e) {
    wrapKeyboardSelect(() => {
      e.preventDefault();
      e.stopPropagation();
      this.setState({ zooming: true });
    }, e);
  }

  onClick(e) {
    wrapKeyboardSelect(() => {
      if (e.target === this.refs.icon || e.target === this.refs.zoom) {
        // Do not apply default click action to icon
        return;
      }

      const { content, editingAny } = this.props;
      e.stopPropagation();
      if (editingAny) {
        e.preventDefault();
      } else if (content && this.getContentInstance().constructor.overridesOnClick()) {
        this.getContentInstance().onClick();
      } else if (content && content.href) {
        if (window.navigator.userAgent.indexOf('MyWarwick/') >= 0) {
          window.location = content.href;
        } else {
          window.open(content.href);
        }
      } else if (this.getContentInstance().constructor.expandsOnClick()) {
        this.props.onZoomIn();
      }
    }, e);
  }

  componentDidHide() {
    $(document.body).removeClass('tile-zooming');
    this.setState({ zooming: false });
  }

  componentDidShow() {
    // Trigger an immediate re-render after the initial mount so we can access the content instance

    this.setState({ // eslint-disable-line react/no-did-mount-set-state
      hasMounted: true,
    });
  }

  componentDidUpdate(prevProps, prevState) {
    if (!prevState.zooming && this.state.zooming) {
      const $articleElement = $(this.articleElement);
      $articleElement.on('transitionend.mywarwick', () => {
        $articleElement.off('transitionend.mywarwick');
        this.props.onZoomIn();
      });
      $(document.body).addClass('tile-zooming');
    }
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
        return <i ref="zoom" className="fas fa-expand-alt" role="button" tabIndex={0} onClick={this.onClickExpand} onKeyUp={this.onClickExpand} />;
      }
      return null;
    };

    const tileSizeClasses = supportedTileSizes.map(s => `tile-size-supported--${s}`);

    const clickProps = (content && content.href) ? {
      onClick: this.onClick,
      onKeyUp: this.onClick,
      role: 'button',
      tabIndex: 0,
    } : {
      onClick: this.onClick,
      onKeyUp: this.onClick,
    };

    const style = {};
    if (this.state.zooming && this.articleElement) {
      const thisOffset = $(this.articleElement).offset();
      const meContainerOffset = $(this.articleElement).closest('.me-view-container').offset();
      const scrollTop = $(document.body).prop('scrollTop');
      style.top = (meContainerOffset.top - thisOffset.top) + scrollTop;
      style.left = meContainerOffset.left - thisOffset.left;
    }

    return (
      <div className={`tile__container tile--${type}__container`}>
        <article
          { ...clickProps }
          className={
            classNames(
              'tile', `tile--${type}`, `tile--${size}`, `colour-${colour}`,
              {
                'tile--editing': editing,
                'tile--zoomed': zoomed || this.state.zooming,
              }, tileSizeClasses,
            )
          }
          ref={ (article) => { this.articleElement = article; }}
          style={ style }
        >
          { this.getContentInstance() && this.getContentInstance().constructor.isRemovable() &&
            <div
              className="tile__edit-control top-left"
              onClick={ this.props.onHide }
              onKeyUp={ this.props.onHide }
              role="button"
              tabIndex={0}
              title={ `Hide ${title}` }
            >
              <div className="icon"><i className="fal fa-minus" /></div>
            </div>
          }

          { this.getContentInstance() && this.getContentInstance().constructor.isMovable() &&
            <div
              className="tile__edit-control bottom-right"
              onClick={ this.props.onResize }
              onKeyUp={ this.props.onResize }
              role="button"
              tabIndex={0}
              title={`Make tile ${_.last(supportedTileSizes) === size ? 'smaller' : 'bigger'}`}
            >
              <div className="icon"><i className="fal fa-arrow-up" /></div>
            </div>
          }

          { this.getContentInstance() && this.getContentInstance().constructor.isMovable() &&
            <div
              className="tile__edit-control bottom-left tile__drag-handle"
              title="Drag to re-arrange tile"
            >
              <div className="icon"><i className="fal fa-arrows-alt" /></div>
            </div>
          }

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
