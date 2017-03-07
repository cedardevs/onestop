import { connect } from 'react-redux'
import { showCollections, toggleGranuleFocus } from '../../../actions/FlowActions'
import { incrementGranulesOffset, fetchGranules } from '../../../actions/SearchRequestActions'
import GranuleList from './GranuleListComponent'

const mapStateToProps = (state) => {
  const id = state.behavior.search.selectedIds[0]
  const { collections, granules, totalGranules } = state.domain.results
  return {
    results: granules,
    focusedIds: state.ui.granuleDetails.focusedGranules,
    selectedCollection: id && collections && collections[id] || {},
    totalHits: totalGranules
  }
}

const mapDispatchToProps = (dispatch) => {
  return {
    toggleFocus: (id) => dispatch(toggleGranuleFocus(id)),
    showCollections: () => dispatch(showCollections()),
    fetchMoreResults: () => {
      dispatch(incrementGranulesOffset())
      dispatch(fetchGranules(false))
    }
  }
}

const GranuleListContainer = connect(
  mapStateToProps,
  mapDispatchToProps
)(GranuleList)

export default GranuleListContainer
