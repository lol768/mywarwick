import React from 'react';
import * as PropTypes from 'prop-types';

/**
 * From https://reacttraining.com/react-router/web/guides/code-splitting
 *
 * A lazy-loading component, which combines with lazy module loading
 * to allow Webpack to split all of a component's code out into a separate
 * file that can be loaded on demand.
 *
 * Various caveats: if you import the component's code normally elsewhere,
 * that's going to pull it into the main bundle.
 *
 * @param load the function that lazy-loads the module.
 *        usually using dynamic import() returning a Promise for the module
 * @param initialise if your module needs one-time inialisation,
 *        it will run this and set the result as state.initial
 */
class Bundle extends React.Component {

  constructor() {
    super();
    this.state = {
      // short for "module" but that's a keyword in js, so "mod"
      mod: null,
    };
  }

  componentWillMount() {
    this.load(this.props);
  }

  componentWillReceiveProps(nextProps) {
    if (nextProps.load !== this.props.load) {
      this.load(nextProps);
    }
  }

  load(props) {
    this.setState({
      mod: null,
      initial: null,
    });
    props.load().then((mod) => {
      // handle both es imports and cjs
      const newMod = mod.default ? mod.default : mod;
      this.setState({
        mod: newMod,
        initial: props.initialise ? props.initialise(newMod) : null,
      });
    });
  }

  render() {
    return this.props.children(this.state.mod, this.state.initial);
  }
}

Bundle.propTypes = {
  load: PropTypes.func.isRequired,
  initialise: PropTypes.func,
  children: PropTypes.func.isRequired,
};

export default Bundle;
