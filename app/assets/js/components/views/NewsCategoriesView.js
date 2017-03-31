import React, { PropTypes } from 'react';
import ReactDOM from 'react-dom';
import Multiselect from 'react-bootstrap-multiselect';
import * as newsCategories from '../../state/news-categories';
import _ from 'lodash-es';
import $ from 'jquery';

export default class NewsCategoriesView extends React.Component {

  constructor(props) {
    super(props);

    this.state = {
      changing: false,
    };

    this.onChange = this.onChange.bind(this);
    this.buttonText = this.buttonText.bind(this);
    this.onDropdownShown = this.onDropdownShown.bind(this);
    this.onDropdownHidden = this.onDropdownHidden.bind(this);
  }

  shouldComponentUpdate(nextProps, nextState) {
    return !(this.state.changing || nextState.changing);
  }

  onChange(options) {
    const { subscribed, dispatch } = this.props;

    this.setState({ changing: true });

    _.each(options, option => {
      const id = option.value;

      if (subscribed.includes(id)) {
        dispatch(newsCategories.unsubscribe(id));
      } else {
        dispatch(newsCategories.subscribe(id));
      }
    });

    this.setState({ changing: false });
  }

  onDropdownShown() {
    const $node = $(ReactDOM.findDOMNode(this));

    const $btn = $node.find('.btn');
    const $tile = $node.parents('.tile');
    const offset = {
      top: $btn.offset().top - $tile.offset().top,
      left: $btn.offset().left - $tile.offset().left,
    };

    // Bring the grid item (stacking context) to the front so the dropdown
    // menu appears in front of other tiles
    $node.parents('.react-grid-item').css({ zIndex: 1 });

    $node.find('.dropdown-menu').css({
      position: 'fixed',
      top: offset.top + $btn.outerHeight(),
      left: offset.left,
    });
  }

  onDropdownHidden() {
    const $node = $(ReactDOM.findDOMNode(this));

    // Revert to natural z position
    $node.parents('.react-grid-item').css({ zIndex: '' });
  }

  buttonText({ length: optionsLen }) {
    const { items: { length: totalLen } } = this.props;
    return `Categories (${optionsLen === totalLen ? 'all' : `${optionsLen}/${totalLen}`})`;
  }

  render() {
    const { items, subscribed } = this.props;

    const data = items.map(item => ({
      value: item.id,
      label: item.name,
      selected: subscribed.includes(item.id),
    }));

    return (
      <div className="container-fluid news-categories">
        <Multiselect multiple
          data={ data }
          onChange={ this.onChange }
          onDropdownShown={ this.onDropdownShown }
          onDropdownHidden={ this.onDropdownHidden }
          buttonText={ this.buttonText }
        />
      </div>
    );
  }

}

NewsCategoriesView.propTypes = {
  dispatch: PropTypes.func.isRequired,
  items: PropTypes.arrayOf(PropTypes.object).isRequired,
  subscribed: PropTypes.array.isRequired,
};

