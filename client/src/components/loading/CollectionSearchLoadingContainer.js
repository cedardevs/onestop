import {connect} from 'react-redux'
import LoadingBar from './LoadingBar'

import {withRouter} from 'react-router'

const mapStateToProps = state => {
  const {loading} = state.search.collectionRequest.collectionSearchRequestInFlight
  const {collections, totalCollections} = state.search.collectionResult
  const loadedCollections = (collections && Object.keys(collections).length) || 0

  const text = loading ? 'Searching for collections...' : `Loaded ${loadedCollections} of ${totalCollections} collections.`
  const loadingId = `loading-id::${loading}`

  return {
    loading: loading ? 1 : 0,
    loadingText: text,
    loadingAlertId: loadingId,
  }
}

const CollectionSearchLoadingContainer = withRouter(connect(mapStateToProps)(LoadingBar))

export default CollectionSearchLoadingContainer
