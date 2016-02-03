import React from 'react';
import ReactComponent from 'react/lib/ReactComponent';
import $ from 'jquery';

export default class Popover extends ReactComponent {

  constructor(props) {
    super(props);

    this.state = {
      attachPosition: {
        left: 0,
        top: 0,
      },
    };

    this.repositionWithProps = () => this.reposition(this.props);
  }

  componentWillMount() {
    this.reposition(this.props);

    $(window).on('id7:reflow', this.repositionWithProps);
  }

  componentWillUnmount() {
    $(window).off('id7:reflow', this.repositionWithProps);
  }

  componentWillReceiveProps(nextProps) {
    this.reposition(nextProps);
  }

  // Reposition the popover adjacent to the attached element
  reposition(props) {
    if (props.attachTo) {
      const el = $(props.attachTo);

      this.setState({
        attachPosition: {
          left: el.position().left + (el.outerWidth() - props.width) / 2,
          top: el.position().top + el.outerHeight(),
        },
      });
    }
  }

  render() {
    const style = {
      display: 'block',
      left: this.state.attachPosition.left + (this.props.left || 0),
      top: this.state.attachPosition.top + (this.props.top || 0),
      width: this.props.width,
      maxWidth: this.props.width,
    };

    const contentStyle = {
      maxHeight: this.props.height,
      overflowY: this.props.height ? 'auto' : undefined,
      overflowX: 'hidden',
      padding: 0,
    };

    return (
      <div
        className={`popover ${this.props.placement}`} style={style}
      >
        { this.props.arrow ? <div className="arrow"></div> : null }
        { this.props.title ? <div className="popover-title">{ this.props.title }</div> : null }
        <div className="popover-content" style={contentStyle} data-scrollable>
          {this.props.children}
        </div>
        { this.props.onMore ?
          <div
            className="popover-title"
            style={{ borderBottom: 'none', cursor: 'pointer' }}
            onClick={this.props.onMore}
          >
            View All
          </div>
          : null }
      </div>
    );
  }

}
