import React from 'react';
import ReactComponent from 'react/lib/ReactComponent';

export default class UtilityBar extends ReactComponent {

    render() {
        return (
            <ul>
                <li>
                    <a href="#">
                        {this.props.name}
                        <span className="caret"></span>
                    </a>
                </li>
            </ul>
        )
    }

}
