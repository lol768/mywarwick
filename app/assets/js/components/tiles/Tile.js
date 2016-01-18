import React, { Component } from 'react';
import ReactDOM from 'react-dom';

import classNames from 'classnames';

const DEFAULT_TILE_COLOR = '#8c6e96'; // Default ID7 theme colour
const DEFAULT_TEXT_COLOR = 'white';

let sizeClasses = {
  small: 'col-xs-6 col-md-3',
  large: 'col-xs-12 col-sm-6',
  wide: 'col-xs-12 col-sm-6',
  tall: 'col-xs-12 col-sm-6'
};

export default class Tile extends Component {

  getBody(content) {
    throw new TypeError("Must implement getBody");
  }

  canZoom() {
    return false;
  }

  isZoomed() {
    return this.props.zoomed;
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

  render() {
    let props = this.props;

    let icon = props.icon ? <i className={classNames('fa', 'fa-fw', 'fa-' + props.icon)}></i> : null;
    let backgroundColor = props.backgroundColor ? props.backgroundColor : DEFAULT_TILE_COLOR;
    let color = props.color ? props.color : DEFAULT_TEXT_COLOR;

    let size = props.size || props.defaultSize;
    let sizeClass = sizeClasses[size];
    let outerClassName = classNames(sizeClass, {
      'tile--zoomed': props.zoomed,
      'tile--canZoom': this.canZoom(),
      'tile--text-btm': true
    });

    return (
      <div className={outerClassName}>
        <article className={classNames('tile', 'tile--' + props.tileType, 'tile--' + size)}
                 style={{backgroundColor: backgroundColor, color: color}}
                 ref="tile">
          <div className="tile__wrap">
            <header className="tile__title">
              <h1>
                {icon}
                {props.title}
              </h1>
              { this.isZoomed() ?
                <i className="fa fa-times tile__dismiss" onClick={props.onDismiss}></i>
                : this.canZoom() ?
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
    if (this.props.content) {
      return this.isZoomed() ? this.getZoomedBody(this.props.content) : this.getBody(this.props.content);
    } else if (this.props.errors) {
      return (
        <div>
          <i className="fa fa-exclamation-triangle fa-lg"></i>
          <br/>
          {this.props.errors[0].message}
        </div>
      );
    } else {
      return <i className="fa fa-refresh fa-spin fa-lg"></i>;
    }
  }

}
