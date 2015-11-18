import React from 'react';
import ReactDOM from 'react-dom';
import ReactComponent from 'react/lib/ReactComponent';

import SearchField from '../ui/SearchField';
import LinkBlock from '../ui/LinkBlock';
import Link from '../ui/Link';

import { connect } from 'react-redux';
import { fetchSearchResults, clickSearchResult, getRecentItemsOrderedByFrequency } from '../../search';

import $ from 'jquery';
import _ from 'lodash';

export class SearchView extends ReactComponent {

  constructor(props) {
    super(props);

    this.state = {
      searchFocus: false
    };

    this.boundOnReflow = this.onReflow.bind(this);

    this.debouncedOnChange = _.debounce(this.onChange, 300);
  }

  onFocus() {
    this.setState({
      searchFocus: true
    });

    let thisNode = ReactDOM.findDOMNode(this);

    let documentOnClickListener = $(document).on('click', (e) => {
      let parents = $(e.target).parents().get();
      if (parents.indexOf(thisNode) >= 0) return;

      $(document).off('click', documentOnClickListener);

      this.setState({
        searchFocus: false
      });
    });
  }

  componentDidMount() {
    $(window).on('id7:reflow', this.boundOnReflow);
  }

  componentWillUnmount() {
    $(window).off('id7:reflow', this.boundOnReflow);
  }

  onReflow() {
    this.setState({});
  }

  getLinks() {
    let items = this.props.results.length > 0 && this.refs.field.value() == this.props.query ?
      this.props.results :
      getRecentItemsOrderedByFrequency(this.props.recentItems);

    return items.map((result) =>
      <Link key={result.path} href={"http://warwick.ac.uk/" + result.path}
            subtitle={result.path} onClick={() => this.onResultClick(result)}>
        {result.description}
      </Link>
    );
  }

  onChange(value) {
    this.props.dispatch(fetchSearchResults(value));
  }

  onResultClick(result) {
    this.props.dispatch(clickSearchResult(result));
  }

  searchField() {
    return (
      <SearchField onChange={this.debouncedOnChange.bind(this)}
                   onFocus={this.onFocus.bind(this)}
                   ref="field"/>
    );
  }

  quickLinks() {
    return (
      <div style={{marginTop: 20}}>
        <h4>Quick links</h4>
        <LinkBlock columns="2">
          <Link key="bpm" href="http://warwick.ac.uk/bpm">Course Transfers</Link>
          <Link key="ett" href="http://warwick.ac.uk/ett">Exam Timetable</Link>
          <Link key="massmail" href="http://warwick.ac.uk/massmail">Mass Mailing</Link>

          <Link key="mrm" href="http://warwick.ac.uk/mrm">Module Registration</Link>
          <Link key="printercredits" href="http://warwick.ac.uk/printercredits">Printer Credits</Link>
        </LinkBlock>
      </div>
    );
  }

  suggestions() {
    return (
      <LinkBlock columns="1">{this.getLinks()}</LinkBlock>
    );
  }

  render() {
    return (
      <div>
        {this.searchField()}
        {this.state.searchFocus ? this.suggestions() : this.quickLinks()}
      </div>
    );
  }

}

let select = (state) => state.get('search').toJS();

export default connect(select)(SearchView);
