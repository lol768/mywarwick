const React = require('react');
const ReactComponent = require('react/lib/ReactComponent');

export default class TabBarItem extends ReactComponent {

    onClick() {
        this.props.onClick(this);
    }

    render() {
        return (
            <li className={this.props.active ? "tab-bar-item tab-bar-item--active" : "tab-bar-item"} onClick={this.onClick.bind(this)}>
                <i className={"fa fa-" + this.props.icon}>
                    <span className="badge">{this.props.badge}</span>
                </i>
                <span className="tab-label">{this.props.title}</span>
            </li>
        );
    }

}
