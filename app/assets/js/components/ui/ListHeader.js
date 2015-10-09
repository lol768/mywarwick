const React = require('react');
const ReactComponent = require('react/lib/ReactComponent');

export default class ListHeader extends ReactComponent {

    render() {
        return (
            <div className="list-header">
                {this.props.title}
            </div>
        );
    }

}
