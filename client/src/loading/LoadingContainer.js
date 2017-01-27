import { connect } from 'react-redux'
import Loading from './LoadingComponent'

const mapStateToProps = (state) => {
  return {
    loading: state.ui.loading ? 1 : 0
  }
}

const LoadingContainer = connect(
    mapStateToProps
)(Loading)

export default LoadingContainer
