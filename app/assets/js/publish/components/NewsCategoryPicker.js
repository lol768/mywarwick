import React from 'react';
import * as PropTypes from 'prop-types';
import _ from 'lodash-es';
import { Checkbox } from '../../components/ui/Checkbox';
import { AudienceIndicator } from './AudienceIndicator';
import ReactDOM from 'react-dom';

export class NewsCategoryPicker extends React.PureComponent {

  static propTypes = {
    newsCategories: PropTypes.arrayOf(PropTypes.shape({
      id: PropTypes.string,
      name: PropTypes.string,
    })),
    audienceIndicator: PropTypes.objectOf(AudienceIndicator),
  };

  constructor(props) {
    super(props);
    this.state = {
      ignoreCategories: false,
    };
    this.handleCategoriesChange = this.handleCategoriesChange.bind(this);
    this.handleIgnore = this.handleIgnore.bind(this);
  }

  handleCategoriesChange(value, type, path) {
    console.log(`Handle change! ${value} ${type} ${path}`);
  }

  handleIgnore(value, type, path) {
    console.log(`Handle ignore! ${value} ${type} ${path}`);
    this.setState({
      ignoreCategories: !value
    });
  }

  makeOptions(newsCategories) {
    return <div>
      {
        _.map(newsCategories, (name, id) => (
            <Checkbox
              key={id}
              handleChange={this.handleCategoriesChange}
              label={name}
              name='categories[]'
              formPath=''
              value={id}
            />
          )
        )
      }
    </div>;
  }

  render() {
    return (
      <div>
        <label className='control-label'>
          What categories should this news to be tagged with?
        </label>
        { this.makeOptions(this.props.newsCategories) }
        <Checkbox
          handleChange={this.handleIgnore}
          name='item.ignoreCategories'
          label={'Show to everyone in the audience, regardless of their category preferences'}
          checked={this.state.ignoreCategories}
          value={this.state.ignoreCategories}
          formPath=''
        />
      </div>
    );
  }
}

export default (NewsCategoryPicker);