import React from 'react';

const DEFAULT_TILE_COLOR = '#8c6e96'; // Default ID7 theme colour
const DEFAULT_TEXT_COLOR = 'white';

let sizeClasses = {
  normal: 'col-xs-6 col-md-3',
  wide: 'col-xs-12 col-sm-6'
};

export default (props) => {
  let icon = props.icon ? <i className={"fa fa-fw fa-" + props.icon}></i> : null;
  let backgroundColor = props.backgroundColor ? props.backgroundColor : DEFAULT_TILE_COLOR;
  let color = props.color ? props.color : DEFAULT_TEXT_COLOR;

  return (
    <div className={sizeClasses[props.size || 'normal']}>
      <article className={"tile " + props.className} data-href={props.href}
               style={{backgroundColor: backgroundColor, color: color}}>
        <header className="tile__title">
          <h1>
            {icon}
            {props.title}
          </h1>
        </header>
        <div className="tile__body">
          {props.children}
        </div>
      </article>
    </div>
  );
};
