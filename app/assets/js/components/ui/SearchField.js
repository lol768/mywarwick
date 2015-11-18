import React from 'react';
import ReactComponent from 'react/lib/ReactComponent';

export default class SearchField extends ReactComponent {

  onChange() {
    if (this.props.onChange)
      this.props.onChange(this.refs.input.value);
  }

  value() {
    return this.refs.input.value;
  }

  render() {
    return (
      <div className="id7-search">
        <div className="form-group">
          <div className="id7-search-box-container">
            <div className="search-container">
              <input ref="input" type="search" className="form-control input-lg" value={this.props.value}
                     onChange={this.onChange.bind(this)} onFocus={this.props.onFocus} onBlur={this.props.onBlur}
                     placeholder="Search Warwick"/>
              <i className="fa fa-search fa-2x"></i>
            </div>
          </div>
        </div>
      </div>
    );
  }

}

