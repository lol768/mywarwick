import React from 'react/addons';

export class Icon extends React.Component {
  render() {
    let clazzes = ['fa'];
    if (this.props.fixedWidth) clazzes.push('fw');
    clazzes.push('fa-'+this.props.name);
    if (this.props.className) clazzes.push(this.props.className);

    return <i className={clazzes.join(' ')}></i>;
  }
}