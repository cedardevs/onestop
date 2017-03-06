import { connect } from 'react-redux'
import { fetchGranules, clearGranules, clearFacets } from '../actions/SearchRequestActions'
import { toggleSelection, clearSelections, updateQuery, updateSearch } from '../actions/SearchParamActions'
import { triggerSearch } from '../actions/SearchRequestActions'
import { showCollections, showGranules, setFocus } from '../actions/FlowActions'
import Detail from './DetailComponent'

const mapStateToProps = (state, reactProps) => {
  const { focusedId } = state.ui.cardDetails
  const focusedItem = state.domain.results.collections[focusedId]
  const { collections } = state.domain.results
  const geometry = focusedId && collections[focusedId] && collections[focusedId].spatialBounding || '' 
  return {
    geometry: geometry,
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
      dispatch(updateSearch())
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
