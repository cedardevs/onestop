import {connect} from 'react-redux'
import {asyncMoreCollectionResults} from '../../../actions/routing/CollectionSearchRouteActions'
import {showDetails} from '../../../actions/routing/CollectionDetailRouteActions'
import CollectionGrid from './CollectionGrid' // TODO this doesn't even exist?

import {withRouter} from 'react-router'

const mapStateToProps = state => {
  const {
    collections,
    totalCollectionCount,
    loadedCollectionCount,
    pageSize,
  } = state.search.collectionResult
  return {
    loading: state.search.loading ? 1 : 0,
    results: collections,
    totalHits: totalCollectionCount,
    returnedHits: loadedCollectionCount,
    pageSize,
  }
}

const mapDispatchToProps = (dispatch, ownProps) => {
  return {
    selectCollection: id => {
      dispatch(showDetails(ownProps.history, id))
    },
    fetchMoreResults: () => {
      dispatch(asyncMoreCollectionResults())
    },
  }
}

const CollectionGridContainer = withRouter(
  connect(mapStateToProps, mapDispatchToProps)(CollectionGrid)
)

export default CollectionGridContainer
