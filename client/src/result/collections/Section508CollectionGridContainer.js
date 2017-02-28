import { connect } from 'react-redux'
import { showCollections, showGranules, setFocus } from '../../actions/FlowActions'
import { fetchGranules, clearGranules, clearFacets } from '../../actions/SearchRequestActions'
import { toggleSelection, clearSelections, updateQuery, clearSearch } from '../../actions/SearchParamActions'
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
    }
  }
}

const CollectionGridContainer = connect(
    mapStateToProps,
    mapDispatchToProps
)(CollectionGrid)

export default CollectionGridContainer
