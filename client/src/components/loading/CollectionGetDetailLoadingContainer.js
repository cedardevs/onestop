import {connect} from 'react-redux'
import LoadingBar from './LoadingBar'

import {withRouter} from 'react-router'

const mapStateToProps = state => {
  const {
    inFlight,
    requestedID,
    errorMessage,
  } = state.search.collectionDetailRequest
  const text = inFlight
    ? `Loading collection with id ${requestedID}` // the id sneakily lives in this boolean for some reason
    : `Completed collection load.` // TODO put collection id
  const loadingId = `loading-id::${requestedID}`

  return {
    loading: inFlight ? 1 : 0,
    loadingText: text,
    loadingAlertId: loadingId,
    error: errorMessage,
  }
}

const CollectionGetDetailLoadingContainer = withRouter(
  connect(mapStateToProps)(LoadingBar)
)

export default CollectionGetDetailLoadingContainer
