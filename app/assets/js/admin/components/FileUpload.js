import React, { PropTypes } from 'react';
import classnames from 'classnames';
import ProgressBar from '../../components/ui/ProgressBar';
import * as newsImages from '../newsImages';

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

    this.setState({
      uploading: true,
      error: undefined,
    });

    const progress = (loaded, total) => this.setState({ loaded, total });

    newsImages.put(file, progress)
      .then(imageId => {
        this.setState({ imageId });
      })
      .catch((ex) => {
        this.setState({ error: ex.message });
      })
      .then(() => {
        this.setState({
          uploading: false,
          loaded: undefined,
          total: undefined,
        });
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
    const { imageId, uploading, error, loaded, total } = this.state;

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
      <div className={ classnames('form-group', { 'has-error': error }) }>
        <label className="control-label col-md-3" htmlFor={ inputName }>Choose an image</label>

        <div className="col-md-9">
          { error ?
            <p className="help-block">
              { error }
            </p>
            : null }

          <input type="file" id={ inputName } onChange={this.onChange}
            disabled={ uploading } accept="image/*"
          />

          { uploading ?
            <div>
              <p>
                Uploading, please wait&hellip;
              </p>
              <ProgressBar value={ loaded } max={ total } />
            </div>
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
