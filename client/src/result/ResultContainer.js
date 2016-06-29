import { connect } from 'react-redux'
import ResultsList from './ResultListComponent'

const mapStateToProps = (state) => {
  return {
    loading: state.getIn(['search', 'inFlight']),
    results: state.get('results')
  }
}

const ResultsContainer = connect(
    mapStateToProps
)(ResultsList)

export default ResultsContainer
