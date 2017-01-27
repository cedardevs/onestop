import { connect } from 'react-redux'
import { toggleGranuleFocus } from '../../../actions/FlowActions'
import GranuleList from './GranuleListComponent'

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
