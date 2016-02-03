import React, { Component } from 'react';
import ReactDOM from 'react-dom';

import { localMoment } from '../../dateFormatter.js';
import classNames from 'classnames';

import $ from 'jquery';

let sizeClasses = {
  small: 'col-xs-6 col-md-3',
  large: 'col-xs-12 col-sm-6',
  wide: 'col-xs-12 col-sm-6',
  tall: 'col-xs-12 col-sm-6'
};

const LONG_PRESS_DURATION_MS = 600;

export default class Tile extends Component {

  contentOrDefault(content, contentFunction) {
    const defaultText = (content.defaultText === undefined) ? 'Nothing to show.' : content.defaultText;
    if(!content.items || content.items.length === 0) {
      return <span>{defaultText}</span>
    } else {
      return contentFunction.call(this, content);
    }
  }

  getBodyInternal(content) {
    return this.contentOrDefault(content, this.getBody);
  }

  getBody(content) {
    throw new TypeError("Must implement getBody");
  }

  canZoom() {
    return false;
  }

  canReallyZoom() {
    return this.props.editing ? false : this.canZoom();
  }

  isZoomed() {
    return this.props.zoomed;
  }

  getZoomedBodyInternal(content) {
    return this.contentOrDefault(content, this.getZoomedBody);
  }

  getZoomedBody(content) {
    return this.getBody(content);
  }

  componentWillEnter(callback) {
    if ('componentWillEnter' in this.props)
      this.props.componentWillEnter(callback);
  }

  componentWillLeave(callback) {
    if ('componentWillLeave' in this.props)
      this.props.componentWillLeave(callback);
  }

  getIcon() {
    let { fetching, errors, icon } = this.props;

    if (fetching) {
      return 'fa-refresh fa-spin';
    } else if (errors) {
      return 'fa-exclamation-triangle';
    } else if (icon) {
      return `fa-${icon}`;
    } else {
      return 'fa-question-circle';
    }
  }

  getIconTitle() {
    const { fetching, errors, title } = this.props;

    if (fetching) {
      return `Refreshing ${title}`;
    } else if (errors) {
      return `An error occurred while refreshing the ${title} tile. The error was: ${errors[0].message}`;
    } else {
      return `Refresh ${title}`;
    }
  }

  onMouseDown() {
    if (!this.props.editing && !this.isZoomed()) {
      let el = $(ReactDOM.findDOMNode(this.refs.tile));
      el.stop().transition({
        scale: 0.97
      }, 200);

      this.timeout = setTimeout(this.props.onBeginEditing, LONG_PRESS_DURATION_MS);
    }
  }

  onMouseUp() {
    if (!this.props.editing && !this.isZoomed()) {
      let el = $(ReactDOM.findDOMNode(this.refs.tile));
      el.stop().transition({
        scale: 1
      }, 200);

      this.timeout = clearTimeout(this.timeout);
    }
  }

  componentDidUpdate(prevProps) {
    if (this.props.editing && !prevProps.editing) {
      let el = $(ReactDOM.findDOMNode(this.refs.tile));

      el.stop().transition({
        scale: 1.15
      }, 600, 'snap');
    } else if (prevProps.editing && !this.props.editing) {
      let el = $(ReactDOM.findDOMNode(this.refs.tile));

      el.stop().transition({
        scale: 1
      }, 600, 'snap');
    }
  }

  getResizeControlIcon() {
    return {
      small: 'right',
      wide: 'down',
      large: 'up'
    }[this.props.size || this.props.defaultSize];
  }

  render() {
    let props = this.props;

    let icon = <i className={'tile__icon fa fa-fw ' + this.getIcon()} title={this.getIconTitle()} onClick={this.props.onClickRefresh}></i>;

    let size = props.size || props.defaultSize;
    let sizeClass = sizeClasses[size];
    let outerClassName = classNames(sizeClass, {
      'tile--zoomed': props.zoomed,
      'tile--canZoom': this.canReallyZoom(),
      'tile--text-btm': true
    });

    return (
      <div className={outerClassName}>
        <article
          className={classNames('tile', 'tile--' + props.tileType, 'tile--' + size, 'colour-' + props.colour, {'tile--editing': props.editing})}
          onMouseDown={this.onMouseDown.bind(this)} onMouseUp={this.onMouseUp.bind(this)} onMouseOut={this.onMouseUp.bind(this)}
          onTouchStart={this.onMouseDown.bind(this)} onTouchEnd={this.onMouseUp.bind(this)} onTouchCancel={this.onMouseUp.bind(this)}
          ref="tile">
          <div className="tile__edit-control top-left" onClick={this.props.onFinishEditing.bind(this)}>
            <i className="fa fa-fw fa-times"></i>
          </div>
          <div className="tile__edit-control top-right">
            <i className="fa fa-fw fa-ellipsis-v"></i>
          </div>
          <div className="tile__edit-control bottom-right">
            <i className={`fa fa-fw fa-arrow-${this.getResizeControlIcon()}`}></i>
          </div>
          <div className="tile__wrap">
            <header>
              <h1>
                {icon}
                <span className="tile__title">
                  {props.title}
                </span>
              </h1>
              { this.isZoomed() ?
                <i className="fa fa-times tile__dismiss" onClick={props.onDismiss}></i>
                : this.canReallyZoom() ?
                <i className="fa fa-expand tile__expand" onClick={props.onExpand}></i> : null }
            </header>
            <div className="tile__body">
              {this.getOuterBody()}
            </div>
          </div>
        </article>
      </div>
    );
  }

  getOuterBody() {
    const { content, errors, zoomed, fetchedAt } = this.props;

    if (content) {
      let body = zoomed ? this.getZoomedBodyInternal(content) : this.getBodyInternal(content);

      if (errors) {
        return (
          <div>
            <div className="tile__last-updated">
              Last updated {localMoment(fetchedAt).calendar()}
            </div>
            {body}
          </div>
        );
      } else {
        return (
          <div>
            {body}
          </div>
        );
      }
    } else {
      // Initial load
      return null;
    }
  }

}
