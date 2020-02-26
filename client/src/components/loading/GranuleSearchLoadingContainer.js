import {connect} from 'react-redux'
import LoadingBar from './LoadingBar'

import {withRouter} from 'react-router'

const mapStateToProps = state => {
  const {errorMessage} = state.search.granuleRequest

  return {
    error: errorMessage,
  }
}

const GranuleSearchLoadingContainer = withRouter(
  connect(mapStateToProps)(LoadingBar)
)

export default GranuleSearchLoadingContainer
