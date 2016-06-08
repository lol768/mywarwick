import React, { PropTypes } from 'react';
import * as newsImages from '../newsImages';
import $ from 'jquery';

export default class FileUpload extends React.Component {

  constructor(props) {
    super(props);
    this.state = {};

    this.onChange = this.onChange.bind(this);
    this.onClear = this.onClear.bind(this);
  }

  onChange(e) {
    const fileField = e.target;
    const file = fileField.files[0];
    const $submitBtn = $('button[type=submit]');
    const submitText = $submitBtn.text();

    this.setState({
      uploading: true,
    });
    $submitBtn.prop('disabled', true).text('Uploading file...');

    newsImages.put(file)
      .then(imageId => {
        this.setState({ imageId });
      })
      .catch((ex) => {
        alert(`There was a problem uploading the image: ${ex.message}`); // eslint-disable-line no-alert, max-len
      })
      .then(() => {
        this.setState({
          uploading: false,
        });
        $submitBtn.prop('disabled', false).text(submitText);
        fileField.value = '';
      });
  }

  onClear() {
    this.setState({
      imageId: undefined,
    });
  }

  render() {
    const { inputName } = this.props;
    const { imageId, uploading } = this.state;

    if (imageId) {
      return (
        <div className="form-group">
          <label className="control-label col-md-3">Choose an image</label>

          <div className="col-md-9">
            <div>
              <ImagePreview imageId={ imageId } width={ 300 } />
            </div>

            <div>
              <button onClick={ this.onClear } className="btn btn-default">
                Use a different image
              </button>
            </div>
          </div>

          <input type="hidden" name={ inputName } value={ imageId } />
        </div>
      );
    }

    return (
      <div className="form-group">
        <label className="control-label col-md-3" htmlFor={ inputName }>Choose an image</label>

        <div className="col-md-9">
          <input type="file" id={ inputName } onChange={this.onChange}
            disabled={ uploading } accept="image/*"
          />
          { uploading ?
            <div>Uploading, please wait&hellip;</div>
            : null }
        </div>
      </div>
    );
  }

}

FileUpload.propTypes = {
  inputName: PropTypes.string.isRequired,
};

const ImagePreview = ({ imageId, width }) => {
  let src = `${newsImages.API_BASE}/${imageId}`;

  if (width !== undefined) {
    src = `${src}?width=${width}`;
  }

  return <img src={ src } alt="Preview" width={ width } />;
};

ImagePreview.propTypes = {
  imageId: PropTypes.string.isRequired,
  width: PropTypes.number,
};
