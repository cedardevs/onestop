import { connect } from 'react-redux'
import {
  showCollections,
  showGranules,
  setFocus,
} from '../../actions/FlowActions'
import {
  incrementCollectionsOffset,
  triggerSearch,
  fetchGranules,
  clearCollections,
  clearGranules,
} from '../../actions/SearchRequestActions'
import {
  toggleSelection,
  clearSelections,
  updateQuery,
  updateSearch,
} from '../../actions/SearchParamActions'
import Section508CollectionGrid from './Section508CollectionGrid'

const mapStateToProps = state => {
  const { collections, totalCollections, pageSize } = state.domain.results
  return {
    results: collections,
    totalHits: totalCollections,
    returnedHits: (collections && Object.keys(collections).length) || 0,
    pageSize,
  }
}

const mapDispatchToProps = dispatch => {
  return {
    onCardClick: id => {
      dispatch(setFocus(id))
    },
    showGranules: id => {
      dispatch(setFocus(null))
      dispatch(clearSelections())
      dispatch(toggleSelection(id))
      dispatch(clearGranules())
      dispatch(fetchGranules())
      dispatch(showGranules('508'))
    },
    textSearch: text => {
      dispatch(updateSearch())
      dispatch(updateQuery(text))
      dispatch(clearCollections())
      dispatch(triggerSearch())
      dispatch(showCollections('508'))
    },
    fetchMoreResults: () => {
      dispatch(incrementCollectionsOffset())
      dispatch(triggerSearch(false))
    },
  }
}

const Section508CollectionGridContainer = connect(
  mapStateToProps,
  mapDispatchToProps
)(Section508CollectionGrid)

export default Section508CollectionGridContainer
