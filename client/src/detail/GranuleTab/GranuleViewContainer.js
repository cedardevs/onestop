import { connect } from 'react-redux'
import { toggleGranuleFocus } from '../../actions/FlowActions'
import { incrementGranulesOffset, fetchGranules } from '../../actions/SearchRequestActions'
import GranuleView from './GranuleView'

const mapStateToProps = (state, reactProps) => {
  const id = state.behavior.search.selectedIds[0]
  const {granules, totalGranules} = state.domain.results
  return {
    results: granules,
    totalHits: totalGranules
  }
}

const mapDispatchToProps = (dispatch) => {
  return {
    toggleFocus: (id, bool) => dispatch(toggleGranuleFocus(id, bool)),
    fetchMoreResults: () => {
      dispatch(incrementGranulesOffset())
      dispatch(fetchGranules(false))
    }
  }
}

const GranuleViewContainer = connect(
    mapStateToProps,
    mapDispatchToProps
)(GranuleView)

export default GranuleViewContainer
