import React from 'react';
import ReactDOM from 'react-dom';
import ReactComponent from 'react/lib/ReactComponent';

import $ from 'jquery';

const DEFAULT_TILE_COLOR = '#8c6e96'; // Default ID7 theme colour
const DEFAULT_TEXT_COLOR = 'white';

let sizeClasses = {
  normal: 'col-xs-6 col-md-3',
  wide: 'col-xs-12 col-sm-6'
};

export default class Tile extends ReactComponent {

  constructor(props) {
    super(props);

    this.boundOnReposition = this.onReposition.bind(this);
  }

  componentDidMount() {
    this.onReposition();

    $(window).on('resize', this.boundOnReposition);
  }

  componentWillUnmount() {
    $(window).off('resize', this.boundOnReposition);
  }

  onReposition() {
    let $this = $(ReactDOM.findDOMNode(this));

    this.setState({
      naturalOuterWidth: $this.outerWidth(),
      naturalOuterHeight: $this.outerHeight(),
      originalOffset: $this.offset()
    });
  }

  render() {
    let props = this.props;

    let icon = props.icon ? <i className={"fa fa-fw fa-" + props.icon}></i> : null;
    let backgroundColor = props.backgroundColor ? props.backgroundColor : DEFAULT_TILE_COLOR;
    let color = props.color ? props.color : DEFAULT_TEXT_COLOR;

    let outerClassName = props.zoomed ? 'tile--zoomed' : ('tile--normal ' + sizeClasses[props.size || 'normal']);

    return (
      <div className={outerClassName}>
        <article className={"tile " + props.className}
                 style={{backgroundColor: backgroundColor, color: color}}
                 onClick={props.onClick}>
          <div className="tile__wrap">
            <header className="tile__title">
              <h1>
                {icon}
                {props.title}
              </h1>
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
