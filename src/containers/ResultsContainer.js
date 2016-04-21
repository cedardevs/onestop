import { connect } from 'react-redux'
import ResultsList from '../components/ResultsList'
import { getDetails } from '../actions/detail'

const mapStateToProps = (state) => {
  return {
    loading: state.get('inFlight'),
    results: state.get('results').toJS()
  }
};

const mapDispatchToProps = (dispatch) => {
  return {
    onResultClick: (id) => dispatch(getDetails(id))
  };
};

const ResultsContainer = connect(
    mapStateToProps,
    mapDispatchToProps
)(ResultsList);

export default ResultsContainer