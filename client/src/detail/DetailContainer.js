import { connect } from 'react-redux'
import { setFocus } from '../actions/DetailActions'
import { fetchGranules, clearGranules } from '../actions/SearchActions'
import { toggleSelection, clearSelections } from '../result/collections/CollectionsActions'
import { clearSearch, triggerSearch, updateQuery } from '../actions/SearchActions'
import { clearFacets } from '../search/facet/FacetActions'
import { showCollections, showGranules } from '../actions/FlowActions'
import Detail from './DetailComponent'

const mapStateToProps = (state, reactProps) => {
  const { focusedId } = state.ui.cardDetails
  const focusedItem = state.domain.results.collections[focusedId]
  return {
    id: focusedId,
    item: focusedItem
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
      dispatch(showCollections())
    },
    showGranules: (id) => {
      dispatch(setFocus(null))
      dispatch(clearSelections())
      dispatch(toggleSelection(id))
      dispatch(clearGranules())
      dispatch(fetchGranules())
      dispatch(showGranules())
    }
  }
}

const DetailContainer = connect(
    mapStateToProps,
    mapDispatchToProps
)(Detail)

export default DetailContainer
