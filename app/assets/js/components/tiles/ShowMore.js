// @flow
import * as React from 'react';

type Props = {
  items: any[],
  showing: number,
  onClick: (SyntheticEvent<HTMLElement>) => void
};

export default class ShowMore extends React.PureComponent<Props> {
  props: Props;

  render() {
    const { items, showing } = this.props;
    if (items.length > showing) {
      return (
        <div className="text-right">
          <a
            role="button"
            tabIndex={0}
            onClick={ this.props.onClick }
            onKeyUp={ this.props.onClick }
          >
            +{ items.length - showing } more
          </a>
        </div>
      );
    }
    return null;
  }
}
