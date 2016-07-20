import $ from 'jquery';

import WarwickSearch from 'warwick-search-frontend';

const searchRootUrl = $('#app-container').attr('data-search-root-url');

export default WarwickSearch(searchRootUrl);
