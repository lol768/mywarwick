import React from 'react';
import ReactComponent from 'react/lib/ReactComponent';

import SearchField from './SearchField';
import Popover from './Popover';
import LinkBlock from './LinkBlock';
import Link from './Link';

export default class MastheadSearch extends ReactComponent {

  constructor(props) {
    super(props);

    this.state = {
      popover: false
    };
  }

  onFocus() {
    this.setState({
      popover: true
    });
  }

  onBlur() {
    this.setState({
      popover: false
    });
  }

  onChange() {

  }

  render() {
    return (
      <div className="id7-search-column">
        <SearchField onFocus={this.onFocus.bind(this)}
                     onBlur={this.onBlur.bind(this)}
                     onChange={this.onChange.bind(this)}
                     ref="field"/>

        { this.state.popover ?
          <Popover attachTo={this.refs.field.refs.input} top={40} left={10} width={300} height={300}>
            <LinkBlock columns="1">
              <Link key="a" href="http://">Recent 1</Link>
              <Link key="b" href="http://">Recent 2</Link>
              <Link key="c" href="http://">Recent 3</Link>
              <Link key="d" href="http://">Recent 4</Link>
              <Link key="e" href="http://">Recent 5</Link>
            </LinkBlock>
          </Popover>
          : null}
      </div>
    );
  }

}

