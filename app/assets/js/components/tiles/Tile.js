const React = require('react');

const Tile = (props) => (
    <article className="tile">
        <a href={props.href} target="_blank">
            <div className="tile-body">
                <header className="tile-title">
                    <h1>{props.title}</h1>
                </header>
                {props.children}
            </div>
        </a>
    </article>
);

export default Tile;
