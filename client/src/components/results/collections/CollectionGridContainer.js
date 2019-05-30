import {connect} from 'react-redux'
import {submitCollectionSearchNextPage} from '../../../actions/routing/CollectionSearchRouteActions'
import {submitCollectionDetailAndUpdateUrl} from '../../../actions/routing/CollectionDetailRouteActions'
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
    collectionDetailFilter: state.search.collectionFilter, // just used to submit collection detail correctly
    pageSize,
  }
}

const mapDispatchToProps = (dispatch, ownProps) => {
  return {
    selectCollection: (id, filterState) => {
      dispatch(
        submitCollectionDetailAndUpdateUrl(ownProps.history, id, filterState)
      )
    },
    fetchMoreResults: () => {
      dispatch(submitCollectionSearchNextPage())
    },
  }
}

const CollectionGridContainer = withRouter(
  connect(mapStateToProps, mapDispatchToProps)(CollectionGrid)
)

export default CollectionGridContainer
