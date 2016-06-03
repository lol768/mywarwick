import React, { PropTypes } from 'react';

export default class SearchField extends React.Component {

  constructor(props) {
    super(props);

    this.onChange = this.onChange.bind(this);
    this.onSubmit = this.onSubmit.bind(this);
    this.onClickSearch = this.onClickSearch.bind(this);
  }

  onSubmit(e) {
    e.preventDefault(0);

    if (this.props.onSearch && this.hasQuery()) {
      this.props.onSearch(this.value());
    }
  }

  onClickSearch() {
    if (this.props.onSearch && this.hasQuery()) {
      this.props.onSearch(this.value());
    }
  }

  onChange() {
    if (this.props.onChange) {
      this.props.onChange(this.value());
    }
  }

  hasQuery() {
    return this.value().trim().length > 0;
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
              <form onSubmit={ this.onSubmit }>
                <input
                  ref="input" type="search" className="form-control input-lg"
                  value={ this.props.value } onChange={ this.onChange }
                  onFocus={ this.props.onFocus } onBlur={ this.props.onBlur }
                  placeholder="Search Warwick"
                />
                <i className="fa fa-search fa-2x" onClick={ this.onClickSearch }> </i>
              </form>
            </div>
          </div>
        </div>
      </div>
    );
  }

}

SearchField.propTypes = {
  value: PropTypes.string,
  onFocus: PropTypes.func,
  onBlur: PropTypes.func,
  onSearch: PropTypes.func,
  onChange: PropTypes.func,
};

