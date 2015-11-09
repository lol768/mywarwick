import React from 'react';
import ReactComponent from 'react/lib/ReactComponent';

export default class ID7SearchColumn extends ReactComponent {

  render() {
    return (
      <div className="id7-search-column">
        <div className="id7-search">
          <form action="https://search.warwick.ac.uk/website" role="search">
            <input type="hidden" name="source" value="http://warwick.ac.uk/"/>
            <div className="form-group">
              <label className="sr-only" for="id7-search-box">Search</label>
              <div className="id7-search-box-container">
                <input type="search" className="form-control input-lg" id="id7-search-box" name="q"
                       placeholder="Search Warwick"/>
                <i className="fa fa-search fa-2x"/>
              </div>
            </div>
          </form>
        </div>
      </div>
    );
  }

}

