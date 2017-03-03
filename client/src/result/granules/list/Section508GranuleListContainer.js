import { connect } from 'react-redux'
import { showCollections, toggleGranuleFocus } from '../../../actions/FlowActions'
import GranuleList from './Section508GranuleListComponent'

const mapStateToProps = (state) => {
  const id = state.behavior.search.selectedIds[0]
  const { collections, granules } = state.domain.results
  return {
    results: granules,
    focusedIds: state.ui.granuleDetails.focusedGranules,
    selectedCollection: id && collections && collections[id] || {}
  }
}

const mapDispatchToProps = (dispatch) => {
  return {
    toggleFocus: (id) => dispatch(toggleGranuleFocus(id)),
    showCollections: () => dispatch(showCollections('508'))
  }
}

const GranuleListContainer = connect(
  mapStateToProps,
  mapDispatchToProps
)(GranuleList)

export default GranuleListContainer
