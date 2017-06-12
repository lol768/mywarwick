import React from 'react';

export default class MastheadSearch extends React.PureComponent {

  render() {
    return (
      <div className="id7-search-column">
        <div className="id7-search">
          <form action="//search.warwick.ac.uk/website" role="search">
            <div className="form-group">
              <label className="sr-only" htmlFor="id7-search-box">Search</label>
              <div className="id7-search-box-container">
                <input type="search" className="form-control input-lg" id="id7-search-box"
                  name="q" placeholder="Search Warwick" data-suggest="go"
                />
                <i className="fa fa-search fa-2x"></i>
              </div>
            </div>
          </form>
        </div>
      </div>
    );
  }

}
