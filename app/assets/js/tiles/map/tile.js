const React = require('react/addons');
const log = require('loglevel');
const TilePanel = require('../../components/tilepanel');
const L = require('leaflet');
const fa = require('../../components/fontawesome');

/**
 * Wraps a Leaflet map as a React component.
 */
class LeafletMap extends React.Component {
  componentDidMount() {
    let options = {
      minZoom: 2,
      maxZoom: 20,
      attributionControl: false,
      layers: [
        L.tileLayer(
          'https://api.tiles.mapbox.com/v4/{id}/{z}/{x}/{y}.png?access_token={accessToken}',
          {
            //attribution: '&copy; <a href="http://openstreetmap.org">OpenStreetMap</a> contributors, <a href="http://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>',
            id: 'nickhowes.49a932e8',
            accessToken: 'pk.eyJ1Ijoibmlja2hvd2VzIiwiYSI6IjZjNGJiZjUzOGRkMmUwYTliOWZhNTNlY2NjYzY0NGUwIn0.u89SrU5vZKY-5Va77eVbSw'
          }
        )
      ]
    };
    let map = this.map = L.map(React.findDOMNode(this.refs.map), options);

    if (this.props.coords) {
      map.setView(this.props.coords, this.props.zoom || 15);
      log.info("Map view set");
    }

    // store callback so it can be unregistered later :|
    this._thisOnClick = this.onClick.bind(this);
    map.on('click', this._thisOnClick);

    this.popup = L.popup();

    map.fitWorld();
  }

  componentWillUnmount() {
    this.map.off('click', this._thisOnClick);
    this.map = null;
  }

  onClick(e) {
    this.popup
      .setLatLng(e.latlng)
      .setContent("You clicked the map at " + e.latlng.toString())
      .openOn(this.map);
  }

  render() {
    return <div ref="map" className="map">Loading&hellip;</div>;
  }
}

export default class MapTile extends React.Component {

  constructor(dataPipe) {
    super();
    this.dataPipe = dataPipe;
    this.state = {};
  }

  render() {
    let submittedText = null;
    if (this.state.submitted) {
      submittedText = <p>Well done</p>;
    }
    return <TilePanel heading={this.props.title} contentClass="">
      <div className="card-item">
        <form onSubmit={this.submitForm.bind(this)}>
          <div className="input-group">
            <input type="text"
                   className="form-control"
                   name="query"
                   placeholder="Find a department, service, building or room" />
            <span className="input-group-btn">
              <button className="btn btn-default" type="submit">
                <fa.Icon fixedWidth={true} name='search' />
              </button>
            </span>
          </div>
          <LeafletMap></LeafletMap>
        </form>
      </div>
    </TilePanel>;
  }

  submitForm(event) {
    this.setState({ submitted: true });
  }

}