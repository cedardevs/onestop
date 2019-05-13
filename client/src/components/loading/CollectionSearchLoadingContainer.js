import {connect} from 'react-redux'
import LoadingBar from './LoadingBar'

import {withRouter} from 'react-router'

const mapStateToProps = state => {
  const {
    loading,
  } = state.search.collectionRequest.collectionSearchRequestInFlight
  const {
    collections,
    totalCollectionCount,
    loadedCollectionCount,
  } = state.search.collectionResult

  const text = loading
    ? 'Searching for files...'
    : `Loaded ${loadedCollectionCount} of ${totalCollectionCount} files.`
  const loadingId = `loading-id::${loading}`

  return {
    loading: loading ? 1 : 0,
    loadingText: text,
    loadingAlertId: loadingId,
  }
}

const CollectionSearchLoadingContainer = withRouter(
  connect(mapStateToProps)(LoadingBar)
)

export default CollectionSearchLoadingContainer
