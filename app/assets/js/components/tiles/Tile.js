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

  getContent() {
    throw new TypeError("Must implement getContent");
  }

  canZoom() {
    return false;
  }

  getZoomedContent() {
    return this.getContent();
  }

  getBody() {
    if (this.props.content) {
      return this.props.zoomed ? this.getZoomedContent(this.props.content) : this.getContent(this.props.content);
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

  render() {
    let props = this.props;

    let icon = props.icon ? <i className={classNames('fa', 'fa-fw', 'fa-' + props.icon)}></i> : null;
    let backgroundColor = props.backgroundColor ? props.backgroundColor : DEFAULT_TILE_COLOR;
    let color = props.color ? props.color : DEFAULT_TEXT_COLOR;

    let size = props.size || props.defaultSize;
    let sizeClass = sizeClasses[size];
    let outerClassName = classNames(sizeClass, {
      'tile--zoomed': props.zoomed,
      'tile--canZoom': props.canZoom,
      'tile--text-btm': true
    });

    return (
      <div className={outerClassName}>
        <article className={classNames('tile', props.tileType, 'tile--' + size)}
                 style={{backgroundColor: backgroundColor, color: color}}>
          <div className="tile__wrap">
            <header className="tile__title">
              <h1>
                {icon}
                {props.title}
              </h1>
              { props.zoomed ?
                <i className="fa fa-fw fa-lg fa-times tile__dismiss" onClick={props.onDismiss}></i>
                : this.canZoom() ?
                <i className="fa fa-fw fa-lg fa-expand tile__expand" onClick={props.onClick}></i> : null }
            </header>
            <div className="tile__body">
              {this.getBody()}
            </div>
          </div>
        </article>
      </div>
    );
  }

}
