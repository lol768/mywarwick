import React, { PropTypes } from 'react';

const SearchView = ({ query }) => {
  let src = '//search-dev.warwick.ac.uk/';

  if (query) {
    src += `?q=${query}`;
  }

  return (
    <div className="iframe-embed">
      <iframe src={ src }></iframe>
    </div>
  );
};

SearchView.propTypes = {
  query: PropTypes.string,
};

export default SearchView;
