import { connect } from 'react-redux'
import { setFocus } from '../detail/DetailActions'
import ResultsList from './ResultListComponent'

const mapStateToProps = (state) => {
  return {
    loading: state.getIn(['search', 'inFlight']),
    results: state.get('results')
  }
}

const mapDispatchToProps = (dispatch) => {
  return {
    onCardClick: (id) => {
      dispatch(setFocus(id))
    }
  }
}

const ResultsListContainer = connect(
    mapStateToProps,
    mapDispatchToProps
)(ResultsList)

export default ResultsListContainer
