import {connect} from 'react-redux'
import {asyncMoreCollectionResults} from '../../../actions/search/CollectionSearchActions'
import {showDetails} from '../../../actions/get/CollectionGetDetailActions'
import {collectionIncrementResultsOffset} from '../../../actions/search/CollectionResultActions'
import CollectionGrid from './CollectionGrid' // TODO this doesn't even exist?

import {withRouter} from 'react-router'

const mapStateToProps = state => {
  const {
    collections,
    totalCollections,
    loadedCollections,
    pageSize,
  } = state.search.collectionResult
  return {
    loading: state.search.loading ? 1 : 0,
    results: collections,
    totalHits: totalCollections,
    returnedHits: loadedCollections,
    pageSize,
  }
}

const mapDispatchToProps = (dispatch, ownProps) => {
  return {
    selectCollection: id => {
      dispatch(showDetails(ownProps.history, id))
    },
    fetchMoreResults: () => {
      dispatch(collectionIncrementResultsOffset())
      dispatch(asyncMoreCollectionResults())
    },
  }
}

const CollectionGridContainer = withRouter(
  connect(mapStateToProps, mapDispatchToProps)(CollectionGrid)
)

export default CollectionGridContainer
