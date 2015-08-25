
const log = require('loglevel');
const TilePanel = require('../../components/tilepanel');
const React = require('react/addons');
const FlipCard = require('react-flipcard');

const PeopleStore = require('./store');

/**
 * Search for people.
 */
export default class PeopleTile extends React.Component {
  constructor(props) {
    super(props);
    this.state = {};

    this.peopleStore = new PeopleStore(this.props.tileId, this.props.dataPipe);
  }

  componentDidMount() {
    this.subscription = this.peopleStore.changes.subscribe(() => {
      log.debug("PeopleTile notified of change from PeopleStore");
      this.setState({results: this.peopleStore.getResults()});
    });
  }

  componentWillUnmount() {
    this.subscription.dispose();
  }

  renderResults() {
    if (this.state && this.state.results) {
      let res = this.state.results;
      return <div className="card-item">
        {res.count} results
        {res.items.map(this.renderResult)}
        </div>;
    }
  }

  renderResult(result, i) {
    return <div key={i}><a href={"mailto:" + result.email}>
      {result.name} ({result.email})
    </a></div>;
  }

  render() {
    return <TilePanel heading={this.props.title} contentClass="">
          <div className="card-item">
            <form onSubmit={this.submitForm.bind(this)}>
              <div className="input-group">
                <input type="text"
                       className="form-control"
                       name="query"
                       placeholder="Enter a name" onChange={this.handleChange.bind(this)} />
                <span className="input-group-btn">
                  <button className="btn btn-default" type="button">
                    <i className="fa fa-fw fa-search"></i>
                  </button>
                </span>
              </div>
            </form>
          </div>
          {this.renderResults()}
        </TilePanel>;
  }

  handleChange(e) {
    this.setState({value: event.target.value});
  }

  submitForm(e) {
    log.debug("People search form submit");
    e.preventDefault();
    let name = this.state.value;
    if (name) {
      this.state.flipped = true;
      this.peopleStore.search(name);
    }
  }
}