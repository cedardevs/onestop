import { connect } from 'react-redux'
import ResultsList from './ResultListComponent'
import { getDetails } from '../detail/DetailActions'

const mapStateToProps = (state) => {
  return {
    loading: state.getIn(['search', 'inFlight']),
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