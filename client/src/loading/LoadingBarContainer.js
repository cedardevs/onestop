import {connect} from 'react-redux'
import LoadingBar from './LoadingBar'

const mapStateToProps = state => {
  const {totalCollections} = state.domain.results
  const {loading} = state.ui

  const text = loading
    ? 'loading'
    : totalCollections ? `${totalCollections} results found` : '0 results found'
  const loadingId = `loading-id::${loading}::${totalCollections}`

  return {
    loading: loading ? 1 : 0,
    loadingText: text,
    loadingAlertId: loadingId,
  }
}

const LoadingBarContainer = connect(mapStateToProps)(LoadingBar)

export default LoadingBarContainer
