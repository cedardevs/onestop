import {connect} from 'react-redux'
import LoadingBar from './LoadingBar'

import {withRouter} from 'react-router'

const mapStateToProps = state => {
  const {loading} = state.search.granuleRequest.granuleSearchRequestInFlight
  const {
    granules,
    totalGranuleCount,
    loadedGranuleCount,
  } = state.search.granuleResult

  const text = loading
    ? 'Searching for files...'
    : `Loaded ${loadedGranuleCount} of ${totalGranuleCount} files.`
  const loadingId = `loading-id::${loading}`

  return {
    loading: loading ? 1 : 0,
    loadingText: text,
    loadingAlertId: loadingId,
  }
}

const GranuleSearchLoadingContainer = withRouter(
  connect(mapStateToProps)(LoadingBar)
)

export default GranuleSearchLoadingContainer
