import {connect} from 'react-redux'
import {withRouter} from 'react-router'

import {submitCollectionSearchNextPage} from '../../../actions/routing/CollectionSearchRouteActions'
import {showDetails} from '../../../actions/routing/CollectionDetailRouteActions'
import Collections from './Collections'

const mapStateToProps = state => {
  const {
    collections,
    totalCollectionCount,
    loadedCollectionCount,
    pageSize,
  } = state.search.collectionResult
  return {
    loading: state.search.loading ? 1 : 0, // TODO gets passed to ListView.jsx (in common/ui)
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
      dispatch(submitCollectionSearchNextPage())
    },
  }
}

const CollectionsContainer = withRouter(
  connect(mapStateToProps, mapDispatchToProps)(Collections)
)

export default CollectionsContainer
