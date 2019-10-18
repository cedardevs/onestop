import {connect} from 'react-redux'
import LoadingBar from './LoadingBar'

import {withRouter} from 'react-router'

const mapStateToProps = state => {
  const {inFlight, errorMessage} = state.search.collectionRequest
  const {
    collections,
    totalCollectionCount,
    loadedCollectionCount,
  } = state.search.collectionResult

  const text = inFlight
    ? 'Searching for files...'
    : `Loaded ${loadedCollectionCount} of ${totalCollectionCount} files.`

  return {
    loading: inFlight ? 1 : 0,
    loadingText: text,
    error: errorMessage,
  }
}

const CollectionSearchLoadingContainer = withRouter(
  connect(mapStateToProps)(LoadingBar)
)

export default CollectionSearchLoadingContainer
