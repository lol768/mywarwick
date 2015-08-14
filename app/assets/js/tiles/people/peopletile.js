
const log = require('loglevel');
const TilePanel = require('../../components/tilepanel');
const React = require('react/addons');
const Dispatcher = require('../../dispatcher').default;
const FlipCard = require('react-flipcard');

const actions = require('./').actions;
const PeopleStore = require('./store');

/**
 * Search for people.
 */
export default class PeopleTile extends React.Component {
  constructor(props) {
    super(props);
    this.state = {};

    this.peopleStore = new PeopleStore(this.props.store);
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
              <input type="text" placeholder="Enter a name" onChange={this.handleChange.bind(this)} />
              <button>Search</button>
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
      Dispatcher.dispatch(actions.search(name));
    }
  }
}