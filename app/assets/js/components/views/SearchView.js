import React, { PropTypes } from 'react';
import $ from 'jquery';

const defaultRootUrl = $('#app-container').attr('data-search-root-url');

const SearchView = (props) => {
  const rootUrl = props.rootUrl || defaultRootUrl;

  let src = `${rootUrl}/embed`;

  if (props.query) {
    src += `?q=${props.query}`;
  }

  return (
    <div className="iframe-embed">
      <iframe src={ src }></iframe>
    </div>
  );
};

SearchView.propTypes = {
  query: PropTypes.string,
  rootUrl: PropTypes.string,
};

export default SearchView;
