import React from 'react';
import * as PropTypes from 'prop-types';
import _ from 'lodash-es';
import { connect } from 'react-redux';

import { Checkbox } from '../../components/ui/Checkbox';

export class NewsCategoryPicker extends React.PureComponent {
  static propTypes = {
    newsCategories: PropTypes.arrayOf(PropTypes.shape({
      id: PropTypes.string,
      name: PropTypes.string,
    })),
    formData: PropTypes.shape({
      chosenCategories: PropTypes.arrayOf(PropTypes.string),
      ignoreCategories: PropTypes.bool,
    }),
    audienceDidUpdate: PropTypes.func.isRequired,
  };

  constructor(props) {
    super(props);

    const formData = _.isEmpty(this.props.formData) ? {
      ignoreCategories: false,
      chosenCategories: [],
    } : this.props.formData;

    this.state = {
      ignoreCategories: Boolean(formData.ignoreCategories),
      chosenCategories: formData.chosenCategories.slice(),
    };
    this.handleCategoriesChange = this.handleCategoriesChange.bind(this);
    this.handleIgnore = this.handleIgnore.bind(this);
    this.updateAudienceIndicator = this.updateAudienceIndicator.bind(this);
  }

  updateAudienceIndicator() {
    this.props.audienceDidUpdate();
  }

  handleCategoriesChange(value) {
    this.setState({
      chosenCategories: _.includes(this.state.chosenCategories, value)
        ? _.remove(this.state.chosenCategories.slice(), id => id !== value)
        : this.state.chosenCategories.slice().concat([value]),
    });
    this.updateAudienceIndicator();
  }

  handleIgnore() {
    this.setState({
      ignoreCategories: !this.state.ignoreCategories,
    });
    this.updateAudienceIndicator();
  }

  makeOptions() {
    return (<div>
      {
        _.map(this.props.newsCategories, (name, id) => (<Checkbox
            key={id.toString()}
            handleChange={this.handleCategoriesChange}
            label={name.toString()}
            name="categories[]"
            formPath=""
            value={id.toString()}
            isChecked={_.includes(this.state.chosenCategories, id)}
          />))
      }
    </div>);
  }

  render() {
    return (
      <div>
        <div className="list-group">
          <label className="control-label">
            What should this news item be tagged with?
          </label>
          {this.makeOptions()}
        </div>
        <div className="list-group">
          <Checkbox
            handleChange={this.handleIgnore}
            name="item.ignoreCategories"
            label="Show to everyone in the audience, regardless of their category preferences"
            isChecked={this.state.ignoreCategories}
            value={this.state.ignoreCategories.toString()}
            formPath=""
          />
          <span className="help-block">Use sparingly!</span>
        </div>
      </div>
    );
  }
}

function mapDispatchToProps(dispatch) {
  return ({
    audienceDidUpdate: () => dispatch({
      type: 'AUDIENCE_UPDATE',
    }),
  });
}

export default connect(null, mapDispatchToProps)(NewsCategoryPicker);
