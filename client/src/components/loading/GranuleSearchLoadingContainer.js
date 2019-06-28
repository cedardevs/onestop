import {connect} from 'react-redux'
import LoadingBar from './LoadingBar'

import {withRouter} from 'react-router'

const mapStateToProps = state => {
  const {inFlight, errorMessage} = state.search.granuleRequest
  const {
    granules,
    totalGranuleCount,
    loadedGranuleCount,
  } = state.search.granuleResult

  const text = inFlight
    ? 'Searching for files...'
    : `Loaded ${loadedGranuleCount} of ${totalGranuleCount} files.`
  const loadingId = `loading-id::${inFlight}`

  return {
    loading: inFlight ? 1 : 0,
    loadingText: text,
    loadingAlertId: loadingId,
    error: errorMessage,
  }
}

const GranuleSearchLoadingContainer = withRouter(
  connect(mapStateToProps)(LoadingBar)
)

export default GranuleSearchLoadingContainer
