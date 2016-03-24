/* eslint react/prop-types: 0, react/sort-comp: 0 */
import React, { Component } from 'react';
import ReactDOM from 'react-dom';

import { localMoment } from '../../dateFormatter.js';
import classNames from 'classnames';
import $ from 'jquery';

const SIZE_CLASSES = {
  small: 'col-xs-6 col-sm-6 col-md-3',
  wide: 'col-xs-12 col-sm-12 col-md-6',
  large: 'col-xs-12 col-sm-12 col-md-6',
};

export const EDITING_ANIMATION_DURATION = 600;

export default class Tile extends Component {

  constructor(props) {
    super(props);

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
    const { fetching, errors, icon } = this.props;

    if (fetching) {
      return 'fa-refresh fa-spin';
    } else if (errors) {
      return 'fa-exclamation-triangle';
    } else if (icon) {
      return `fa-${icon}`;
    }
    return 'fa-question-circle';
  }

  getIconTitle() {
    const { errors, fetchedAt } = this.props;

    if (errors) {
      return `Last updated ${localMoment(fetchedAt).calendar()}. ${errors[0].message}`;
    }
  }

  onTouchStart(e) {
    if (!this.props.editing && !this.props.zoomed) {
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

  onClick() {
    const { content, editingAny } = this.props;

    if (!editingAny && content && content.href) {
      if (window.navigator.userAgent.indexOf('Start/') >= 0) {
        window.location = content.href;
      } else {
        window.open(content.href);
      }
    }
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

  animateToScale(scale) {
    const $tile = $(ReactDOM.findDOMNode(this.refs.tile));

    $tile.stop().transition({ scale }, EDITING_ANIMATION_DURATION, 'snap');
  }

  shouldDisplayExpandIcon() {
    return this.props.editing ? false : this.props.canZoom;
  }

  render() {
    const { type, title, size, colour, content, editing, zoomed, isDesktop } = this.props;

    const icon = (<i
      className={`tile__icon fa fa-fw ${this.getIcon()} toggle-tooltip`} ref="icon" title={ this.getIconTitle() }
      data-toggle="tooltip"
    > </i>);

    const sizeClass = SIZE_CLASSES[size];
    const outerClassName =
      classNames({ [`${sizeClass}`]: !zoomed }, 'tile__container', { 'tile--zoomed': zoomed });
    const zoomIcon = () => {
      if (zoomed) {
        return isDesktop ?
          <i className="fa fa-times tile__dismiss" onClick={this.props.onZoomOut}> </i> : null;
      } else if (this.shouldDisplayExpandIcon()) {
        return <i className="fa fa-expand tile__expand" onClick={this.onClickExpand}> </i>;
      }
      return null;
    };

    return (
      <div className={outerClassName}>
        <article
          className={
            classNames(
              'tile', `tile--${type}`, `tile--${size}`, `colour-${colour}`,
              {
                'tile--editing': editing,
                'tile--clickable': content && content.href,
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
            <header>
              <h1>
                {icon}
                <span className="tile__title">
                  {title}
                </span>
              </h1>
              { zoomIcon() }
            </header>
            <div className="tile__body">
              { this.props.children }
            </div>
          </div>
        </article>
      </div>
    );
  }

}
