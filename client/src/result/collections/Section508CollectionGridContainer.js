import { connect } from 'react-redux'
import { showGranules, setFocus } from '../../actions/FlowActions'
import { triggerSearch, fetchGranules, clearGranules } from '../../actions/SearchRequestActions'
import { toggleSelection, clearSelections, updateQuery, updateSearch } from '../../actions/SearchParamActions'
import CollectionGrid from './Section508CollectionGridComponent'

const mapStateToProps = (state) => {
  const { collections, totalCollections } = state.domain.results
  return {
    results: collections,
    totalHits: totalCollections,
    returnedHits: collections && Object.keys(collections).length || 0
  }
}

const mapDispatchToProps = (dispatch) => {
  return {
    onCardClick: (id) => {
      dispatch(setFocus(id))
    },
    showGranules: (id) => {
      dispatch(setFocus(null))
      dispatch(clearSelections())
      dispatch(toggleSelection(id))
      dispatch(clearGranules())
      dispatch(fetchGranules())
      dispatch(showGranules('508'))
    },
    textSearch: (text) => {
      dispatch(updateSearch())
      dispatch(updateQuery(text))
      dispatch(triggerSearch())
    }
  }
}

const CollectionGridContainer = connect(
    mapStateToProps,
    mapDispatchToProps
)(CollectionGrid)

export default CollectionGridContainer
