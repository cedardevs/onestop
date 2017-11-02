import { connect } from 'react-redux'
import { setFocus } from '../../actions/FlowActions'
import { incrementCollectionsOffset, triggerSearch } from '../../actions/SearchRequestActions'
// import CollectionGrid from './CollectionGridComponent'
import CollectionGrid from './CollectionGrid'

const mapStateToProps = (state) => {
  const { collections, totalCollections, pageSize } = state.domain.results
  return {
    results: collections,
    totalHits: totalCollections,
    returnedHits: collections && Object.keys(collections).length || 0,
    pageSize
  }
}

const mapDispatchToProps = (dispatch) => {
  return {
    onCardClick: (id) => {
      dispatch(setFocus(id))
    },
    fetchMoreResults: () => {
      dispatch(incrementCollectionsOffset())
      dispatch(triggerSearch(false))
    }
  }
}

const CollectionGridContainer = connect(
    mapStateToProps,
    mapDispatchToProps
)(CollectionGrid)

export default CollectionGridContainer
