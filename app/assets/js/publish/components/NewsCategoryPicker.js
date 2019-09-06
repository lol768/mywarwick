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
    updateAudience: PropTypes.func.isRequired,
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

    this.props.updateAudience(
      formData.ignoreCategories || formData.chosenCategories.length > 0,
    );
  }


  handleCategoriesChange(value) {
    const chosenCategories = _.includes(this.state.chosenCategories, value)
      ? _.remove(this.state.chosenCategories.slice(), id => id !== value)
      : this.state.chosenCategories.slice().concat([value]);

    this.props.updateAudience(
      this.state.ignoreCategories || chosenCategories.length > 0,
    );
    this.setState({
      chosenCategories,
    });
  }

  handleIgnore() {
    const ignoreCategories = !this.state.ignoreCategories;
    this.props.updateAudience(
      ignoreCategories || this.state.chosenCategories.length > 0,
    );
    this.setState({
      ignoreCategories,
    });
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
      <div className="panel panel-default">
        <div className="panel-heading">
          <h3 className="panel-title">Category tagging</h3>
        </div>
        <div className="panel-body">
          <label className="control-label">
            What should this news item be tagged with?
          </label>
          {this.makeOptions()}

          <hr className="tight" />

          <Checkbox
            handleChange={this.handleIgnore}
            name="item.ignoreCategories"
            label="Show to everyone in the audience, regardless of their category preferences"
            isChecked={this.state.ignoreCategories}
            value={this.state.ignoreCategories.toString()}
            formPath=""
          />
          <p className="small text-muted">Use sparingly!</p>
        </div>
      </div>
    );
  }
}

function mapDispatchToProps(dispatch) {
  return ({
    updateAudience: canEstimateAudience => dispatch({
      type: 'AUDIENCE_UPDATE',
      canEstimateAudience,
    }),
  });
}

export default connect(null, mapDispatchToProps)(NewsCategoryPicker);
