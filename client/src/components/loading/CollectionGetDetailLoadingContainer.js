import {connect} from 'react-redux'
import LoadingBar from './LoadingBar'

import {withRouter} from 'react-router'

const mapStateToProps = state => {
  const {
    collectionDetailRequestInFlight, requestedID,
  } = state.search.collectionDetailRequest
  const {collectionDetail} = state.search.collectionDetailResult
  const text = collectionDetailRequestInFlight
    ? `Loading collection with id ${requestedID}` // the id sneakily lives in this boolean for some reason
    : `Completed collection load.` // TODO put collection id
  const loadingId = `loading-id::${requestedID}`

  return {
    loading: collectionDetailRequestInFlight ? 1 : 0,
    loadingText: text,
    loadingAlertId: loadingId,
  }
}

const CollectionGetDetailLoadingContainer = withRouter(
  connect(mapStateToProps)(LoadingBar)
)

export default CollectionGetDetailLoadingContainer
