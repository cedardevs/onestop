import { connect } from 'react-redux'
import ResultsList from '../components/ResultsList'

const mapStateToProps = (state) => {
  return {
    loading: state.get('inFlight'),
    results: state.get('results').toJS()
  }
};

const mapDispatchToProps = (dispatch) => {
  return {};
};

const ResultsContainer = connect(
    mapStateToProps,
    mapDispatchToProps
)(ResultsList);

export default ResultsContainer