import { connect } from 'react-redux'
// import { fetchGranules, clearGranules } from '../actions/SearchRequestActions'
// import { toggleSelection, clearSelections } from '../actions/SearchParamActions'
// import { triggerSearch } from '../actions/SearchRequestActions'
// import { showGranules } from '../actions/FlowActions'
// import { showGranules, setFocus } from '../actions/FlowActions'
import { toggleGranuleFocus } from '../actions/FlowActions'
import { incrementGranulesOffset, fetchGranules } from '../actions/SearchRequestActions'
import GranuleView from './GranuleView'

const mapStateToProps = (state, reactProps) => {
  // const { focusedId } = state.ui.cardDetails
  // const focusedItem = state.domain.results.collections[focusedId]
  // //const { collections } = state.domain.results
  // //const geometry = focusedId && collections[focusedId] && collections[focusedId].spatialBounding || ''
  // return {
  //   //geometry: geometry,
  //   collectionId: focusedId,
  //   item: focusedItem
  // }
  const id = state.behavior.search.selectedIds[0]
  const { collections, granules, totalGranules } = state.domain.results
  return {
    results: granules,
    focusedIds: state.ui.granuleDetails.focusedGranules,
    selectedCollection: id && collections && collections[id] || {},
    totalHits: totalGranules
  }
}
//
// const mapDispatchToProps = (dispatch) => {
//   return {
//     showGranules: (id) => {
//       // dispatch(setFocus(null))
//       dispatch(clearSelections())
//       dispatch(toggleSelection(id))
//       dispatch(clearGranules())
//       dispatch(fetchGranules())
//       // dispatch(showGranules())
//     }
//   }
// }


const mapDispatchToProps = (dispatch) => {
  return {
    toggleFocus: (id, bool) => dispatch(toggleGranuleFocus(id, bool)),
    // showCollections: () => dispatch(showCollections()),
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
