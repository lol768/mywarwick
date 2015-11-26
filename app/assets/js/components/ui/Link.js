import React from 'react';
import ReactComponent from 'react/lib/ReactComponent';

export default class Link extends ReactComponent {

  render() {
    return (
      <li>
        <a className="link-block__item" href={this.props.href} target="_blank" onClick={this.props.onClick}>
          {
            this.props.subtitle ?
              <span>
            <span className="link-block__item__title">{this.props.children}</span>
            <small className="link-block__item__subtitle">{this.props.subtitle}</small>
        </span>
              :
              this.props.children
          }
        </a>
      </li>
    );
  }

}
