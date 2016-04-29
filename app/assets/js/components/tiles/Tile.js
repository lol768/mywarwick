/* eslint react/prop-types: 0, react/sort-comp: 0 */
import React, { Component } from 'react';

import { localMoment } from '../../dateFormatter.js';
import classNames from 'classnames';

export const EDITING_ANIMATION_DURATION = 600;

export default class Tile extends Component {

  constructor(props) {
    super(props);
    this.state = {
      contentRef: null,
    };

    this.onClick = this.onClick.bind(this);
    this.onClickExpand = this.onClickExpand.bind(this);
    this.onTouchEnd = this.onTouchEnd.bind(this);
    this.onTouchMove = this.onTouchMove.bind(this);
    this.onTouchStart = this.onTouchStart.bind(this);
  }

  componentWillEnter(callback) {
    if ('componentWillEnter' in this.props) {
      this.props.componentWillEnter(callback);
    }
  }

  componentWillLeave(callback) {
    if ('componentWillLeave' in this.props) {
      this.props.componentWillLeave(callback);
    }
  }

  getIcon() {
    const { fetching, errors, icon, content } = this.props;

    // FIXME: shouldn't have to pass content here, the TileContent component has its own content
    const customIcon = (content && this.state.contentRef) ?
      this.state.contentRef.getIcon(content)
      : null;

    const iconJsx = iconName => (
      <i className={`fa ${iconName} toggle-tooltip`} ref="icon" title={ this.getIconTitle() }
        data-toggle="tooltip"
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

  onTouchStart(e) {
    if (!this.props.editingAny && !this.props.zoomed) {
      if (e.changedTouches) {
        const touch = e.changedTouches[0];
        this.startX = touch.clientX;
        this.startY = touch.clientY;
      }

      this.timeout = setTimeout(this.props.onBeginEditing, EDITING_ANIMATION_DURATION);
    }
  }

  onTouchMove(e) {
    if (!this.props.editing && this.startX !== null) {
      const touch = e.changedTouches[0];

      if (Math.abs(touch.clientX - this.startX) > 10
        || Math.abs(touch.clientY - this.startY) > 10) {
        this.release();
      }
    }
  }

  release() {
    this.timeout = clearTimeout(this.timeout);
    this.startX = null;
    this.startY = null;
  }

  onTouchEnd() {
    if (!this.props.editing && !this.props.zoomed && this.timeout) {
      this.release();
    }
  }

  onClickExpand(e) {
    this.release();

    this.props.onZoomIn(e);
  }

  onClick(e) {
    const { content, editingAny } = this.props;
    e.stopPropagation();
    if (editingAny) {
      e.preventDefault();
    } else if (content && content.href) {
      if (window.navigator.userAgent.indexOf('Start/') >= 0) {
        window.location = content.href;
      } else {
        window.open(content.href);
      }
    }
  }

  componentDidMount() {
    if (this.props.editing) {
      this.animateToScale(1.15);
    }
    this.setState({ //eslint-disable-line
      contentRef: this.refs.content,
    });
  }

  componentDidUpdate(prevProps) {
    const nowEditing = this.props.editing;
    const wasEditing = prevProps.editing;

    if (nowEditing && !wasEditing) {
      this.animateToScale(1.15);
    } else if (wasEditing && !nowEditing) {
      this.animateToScale(1);
    }
  }

  animateToScale() {
    /*
    const $tile = $(ReactDOM.findDOMNode(this.refs.tile));

    $tile.stop().transition({ scale }, EDITING_ANIMATION_DURATION, 'snap');
    */
  }

  shouldDisplayExpandIcon() {
    return this.props.editing ? false : this.props.canZoom;
  }

  render() {
    const { type, title, size, colour, content, editing, zoomed, isDesktop } = this.props;

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
          onTouchStart={ this.onTouchStart }
          onTouchMove={ this.onTouchMove }
          onTouchEnd={ this.onTouchEnd }
          onTouchCancel={ this.onTouchEnd }
          onMouseDown={ this.onTouchStart }
          onMouseUp={ this.onTouchEnd }
          onMouseOut={ this.onTouchEnd }
          onClick={ this.onClick }
          ref="tile"
        >
          <div
            className="tile__edit-control top-left"
            onClick={ this.props.onHide }
            title="Hide tile"
          >
            <i className="fa fa-fw fa-minus"> </i>
          </div>
          <div
            className="tile__edit-control bottom-right"
            onClick={ this.props.onResize }
            title={`Make tile ${size !== 'large' ? 'bigger' : 'smaller'}`}
          >
            <i className="fa fa-fw fa-arrow-up"> </i>
          </div>
          <div className="tile__wrap">
            <header className="tile__header">
              <div className="tile__icon tile__icon--left">{this.getIcon()}</div>
              <div className="tile__icon tile__icon--right">{zoomIcon()}</div>
              <div className="tile__title">{title}</div>
            </header>
            <div className="tile__body">
              { React.cloneElement(
                React.Children.only(this.props.children),
                { ref: 'content' }
              )}
            </div>
          </div>
        </article>
      </div>
    );
  }
}
