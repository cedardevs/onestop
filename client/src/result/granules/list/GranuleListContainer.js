import { connect } from 'react-redux'
import { toggleGranuleFocus } from '../GranulesActions'
import GranuleList from './GranuleListComponent'

const mapStateToProps = (state) => {
  const id = state.appState.collectionSelect.selectedIds[0]
  const { collections } = state.domain.results
  return {
    results: state.domain.results.granules,
    focusedIds: state.ui.granuleDetails.focusedGranules,
    selectedCollection: id && collections ? collections[id] : {}
  }
}

const mapDispatchToProps = (dispatch) => {
  return {
    onMouseOver: (id) => {
      dispatch(toggleGranuleFocus(id))
    }
  }
}

const GranuleListContainer = connect(
  mapStateToProps,
  mapDispatchToProps
)(GranuleList)

export default GranuleListContainer
