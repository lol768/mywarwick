import React from 'react';
import ReactDOM from 'react-dom';
import ReactComponent from 'react/lib/ReactComponent';
import store from '../../store';
import classNames from 'classnames';

import $ from 'jquery';

import { fetchTileContent } from '../../serverpipe'
const DEFAULT_TILE_COLOR = '#8c6e96'; // Default ID7 theme colour
const DEFAULT_TEXT_COLOR = 'white';

let sizeClasses = {
  small: 'col-xs-6 col-md-3',
  large: 'col-xs-12 col-sm-6',
  wide: 'col-xs-12 col-sm-6',
  tall: 'col-xs-12 col-sm-6'
};

export default class Tile extends ReactComponent {

  componentDidMount() {
    store.dispatch(fetchTileContent(this.props.id));
  }

  render() {
    let props = this.props;

    let icon = props.icon ? <i className={classNames('fa', 'fa-fw', 'fa-' + props.icon)}></i> : null;
    let backgroundColor = props.backgroundColor ? props.backgroundColor : DEFAULT_TILE_COLOR;
    let color = props.color ? props.color : DEFAULT_TEXT_COLOR;

    let sizeClass = sizeClasses[props.tileConfig.size || props.tileDefaults.defaultSize];
    let outerClassName = classNames({
      'tile--normal': !props.zoomed,
      [sizeClass]: !props.zoomed,
      'tile--zoomed': props.zoomed
    });

    return (
      <div className={outerClassName}>
        <article className={classNames('tile', props.className)}
                 style={{backgroundColor: backgroundColor, color: color}}
                 onClick={props.onClick}
                 ref="tile">
          <div className="tile__wrap">
            <header className="tile__title">
              <h1>
                {icon}
                {props.id}
              </h1>
              { props.zoomed ?
                <i className="fa fa-fw fa-lg fa-times tile__dismiss" onClick={this.props.onDismiss}></i>
                : null }
            </header>
            <div className="tile__body">
              {props.children}
            </div>
          </div>
        </article>
      </div>
    );
  }

}
