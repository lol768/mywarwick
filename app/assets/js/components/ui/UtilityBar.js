import React from 'react';
import ReactComponent from 'react/lib/ReactComponent';

import { connect } from 'react-redux';

export default class UtilityBar extends ReactComponent {

    constructor(props) {
        super(props);
        this.state = {name: props.name};
    }


    render() {
        return (
            <ul>
                <li>
                    <a href="#">
                        {this.state.name}
                        <span className="caret"></span>
                    </a>
                </li>
            </ul>
        )
    }

}
