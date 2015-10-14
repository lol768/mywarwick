const React = require('react');
const ReactComponent = require('react/lib/ReactComponent');

const ProgressBar = require('./ProgressBar');
const AppIcon = require('./AppIcon');
const ApplicationStore = require('../../stores/ApplicationStore');

export default class UpdatePopup extends ReactComponent {

  constructor(props) {
    super(props);

    this.state = {};
  }

  componentDidMount() {
    ApplicationStore.addListener(() => {
      this.setState(ApplicationStore.getUpdateState());
    });
  }

  render() {
    if (!this.state.isUpdating) {
      return <div />;
    }

    if (this.state.loaded < this.state.total || this.state.loaded == 0) {
      return (
        <div className="activity-item" style={{marginBottom: 15}}>
          <div className="media">
            <div className="media-left">
              <AppIcon app="Update" size="lg" />
            </div>
            <div className="media-body" style={{lineHeight: 2}}>
              An update to Start.Warwick is being downloaded.
              <ProgressBar value={this.state.loaded} max={this.state.total} />
            </div>
          </div>
        </div>
      );
    } else {
      return (
        <div className="activity-item" style={{marginBottom: 15}}>
          <div className="media">
            <div className="media-left">
              <AppIcon app="Update" size="lg" />
            </div>
            <div className="media-body" style={{lineHeight: 2}}>
              An update to Start.Warwick is ready to be installed.
            </div>
            <div className="media-right">
              <button className="btn btn-default" onClick={this.reload}>Install now</button>
            </div>
          </div>
        </div>
      );
    }
  }

  reload() {
    window.location.reload();
  }

}
