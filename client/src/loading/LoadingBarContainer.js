import { connect } from 'react-redux'
import LoadingBar from './LoadingBar'

const mapStateToProps = state => {
  return {
    loading: state.ui.loading ? 1 : 0,
  }
}

const LoadingBarContainer = connect(mapStateToProps)(LoadingBar)

export default LoadingBarContainer
