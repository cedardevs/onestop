import {connect} from 'react-redux'
import LoadingBar from './LoadingBar'

import {withRouter} from 'react-router'

const mapStateToProps = state => {
  const {
    loading,
  } = state.search.collectionDetailRequest.collectionDetailRequestInFlight
  const {collectionDetail} = state.search.collectionDetailResult
  // const collectionId = collectionDetail.collection.id // TODO why isn't this set yet when collectionDetailRequestInFlight false? - oh because it defaults to false before a load is ever triggered
  console.log(collectionDetail)
  const text = loading
    ? `Loading collection with id ${loading}` // the id sneakily lives in this boolean for some reason
    : `Completed collection load.` // TODO put collection id
  const loadingId = `loading-id::${loading}`

  return {
    loading: loading ? 1 : 0,
    loadingText: text,
    loadingAlertId: loadingId,
  }
}

const CollectionGetDetailLoadingContainer = withRouter(
  connect(mapStateToProps)(LoadingBar)
)

export default CollectionGetDetailLoadingContainer
