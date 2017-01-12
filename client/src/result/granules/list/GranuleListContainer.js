import { connect } from 'react-redux'
import { toggleGranuleFocus } from '../GranulesActions'
import GranuleList from './GranuleListComponent'

const mapStateToProps = (state) => {
  const id = state.collections.selectedIds[0]
  return {
    results: state.granules.granules,
    focusedIds: state.granules.focusedGranules,
    selectedCollection: state.collections.results[id] || {}
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
