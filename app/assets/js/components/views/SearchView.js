import React from 'react';
import ReactComponent from 'react/lib/ReactComponent';

import SearchField from '../ui/SearchField';
import LinkBlock from '../ui/LinkBlock';
import Link from '../ui/Link';

export default class SearchView extends ReactComponent {

    constructor(props) {
        super(props);

        this.state = {
            query: ''
        };
    }

    onSearchChange(text) {
        this.setState({
            query: text
        });
    }

    onSearchFocus() {
        this.setState({
            searchFocus: true
        });
    }

    onSearchBlur() {
        this.setState({
            searchFocus: false
        });
    }

    render() {
        let links = (
            <div style={{marginTop: '20px'}}>
                <h4>Quick links</h4>
                <LinkBlock columns="3">
                    <Link key="bpm" href="http://warwick.ac.uk/bpm">Course Transfers</Link>
                    <Link key="ett" href="http://warwick.ac.uk/ett">Exam Timetable</Link>
                    <Link key="massmail" href="http://warwick.ac.uk/massmail">Mass Mailing</Link>

                    <Link key="mrm" href="http://warwick.ac.uk/mrm">Module Registration</Link>
                    <Link key="printercredits" href="http://warwick.ac.uk/printercredits">Printer Credits</Link>
                </LinkBlock>
            </div>
        );

        let suggestions = (
            <LinkBlock columns="1">
                <Link key="a" href="http://">Recent 1</Link>
                <Link key="b" href="http://">Recent 2</Link>
                <Link key="c" href="http://">Recent 3</Link>
            </LinkBlock>
        );

        return (
            <div>
                <SearchField value={this.state.query} onChange={this.onSearchChange.bind(this)} onFocus={this.onSearchFocus.bind(this)} onBlur={this.onSearchBlur.bind(this)} />
                {this.state.searchFocus ? suggestions : links}
            </div>
        );
    }
}
