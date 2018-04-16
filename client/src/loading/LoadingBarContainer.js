import {connect} from 'react-redux'
import LoadingBar from './LoadingBar'

const mapStateToProps = state => {
  const {loading} = state.ui

  const text = loading ? 'loading' : 'load complete'
  const loadingId = `loading-id::${loading}`

  return {
    loading: loading ? 1 : 0,
    loadingText: text,
    loadingAlertId: loadingId,
  }
}

const LoadingBarContainer = connect(mapStateToProps)(LoadingBar)

export default LoadingBarContainer
