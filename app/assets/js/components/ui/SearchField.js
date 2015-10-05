const React = require('react');
const ReactComponent = require('react/lib/ReactComponent');

export default class SearchField extends ReactComponent {

    onChange() {
        this.props.onChange(this.refs.searchInput.value);
    }

    render() {
        return (
            <div className="id7-search">
                <div className="form-group">
                    <div className="id7-search-box-container">
                        <div className="search-container">
                            <input ref="searchInput" type="search" className="form-control input-lg" value={this.props.value} onChange={this.onChange.bind(this)} onFocus={this.props.onFocus} onBlur={this.props.onBlur} />

                            <i className="fa fa-search fa-2x"></i>
                        </div>
                    </div>
                </div>
            </div>
        );
    }

}

