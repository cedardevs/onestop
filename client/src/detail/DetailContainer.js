import { connect } from 'react-redux'
import { setFocus } from './DetailActions'
import { fetchGranules, clearGranules } from '../result/granules/GranulesActions'
import { toggleSelection, clearSelections } from '../result/collections/CollectionsActions'
import { clearSearch, triggerSearch, updateQuery } from '../search/SearchActions'
import { clearFacets } from '../search/facet/FacetActions'
import Detail from './DetailComponent'

const mapStateToProps = (reduxState, reactProps) => {
  const focusedId = reduxState.details.focusedId
  const focusedItem = reduxState.collections.results[focusedId]
  return {
    id: focusedId,
    item: focusedItem,
    showGranulesLink: reduxState.config.granuleDetails || true
  }
}

const mapDispatchToProps = (dispatch) => {
  return {
    dismiss: () => dispatch(setFocus(null)),
    textSearch: (text) => {
      dispatch(setFocus(null))
      dispatch(clearFacets())
      dispatch(clearSearch())
      dispatch(updateQuery(text))
      dispatch(triggerSearch())
    },
    showGranules: (id) => {
      dispatch(setFocus(null))
      dispatch(clearSelections())
      dispatch(toggleSelection(id))
      dispatch(clearGranules())
      dispatch(fetchGranules())
    }
  }
}

const DetailContainer = connect(
    mapStateToProps,
    mapDispatchToProps
)(Detail)

export default DetailContainer
