import React from 'React';

const DEFAULT_TILE_COLOR = '#5b3069'; // Warwick Aubergine
const DEFAULT_TEXT_COLOR = 'white';

export default (props) => {
    let icon = props.icon ? <i className={"fa fa-fw fa-" + props.icon}></i> : null;
    let backgroundColor = props.backgroundColor ? props.backgroundColor : DEFAULT_TILE_COLOR;
    let color = props.color ? props.color : DEFAULT_TEXT_COLOR;

    return (
        <article className="tile" style={{backgroundColor: backgroundColor, color: color}}>
            <a href={props.href} target="_blank">
                <div className="tile-body">
                    <header className="tile-title">
                        <h1>
                            {icon}
                            {props.title}
                        </h1>
                    </header>
                    {props.children}
                </div>
            </a>
        </article>
    );
};
