import {connect} from 'react-redux'
import {withRouter} from 'react-router'

import {
  triggerSearch,
  showDetails,
} from '../../../actions/search/CollectionSearchActions'
import {collectionIncrementResultsOffset} from '../../../actions/search/CollectionResultActions'
import Collections from './Collections'

const mapStateToProps = state => {
  const {
    collections,
    totalCollections,
    pageSize,
  } = state.search.collectionResult
  return {
    loading: state.search.loading ? 1 : 0, // TODO gets passed to ListView.jsx (in common/ui)
    results: collections,
    totalHits: totalCollections,
    returnedHits: (collections && Object.keys(collections).length) || 0,
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
      dispatch(triggerSearch(false))
    },
  }
}

const CollectionsContainer = withRouter(
  connect(mapStateToProps, mapDispatchToProps)(Collections)
)

export default CollectionsContainer
