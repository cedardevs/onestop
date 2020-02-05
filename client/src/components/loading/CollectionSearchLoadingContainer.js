import {connect} from 'react-redux'
import LoadingBar from './LoadingBar'

import {withRouter} from 'react-router'

const mapStateToProps = state => {
  const {errorMessage} = state.search.collectionRequest

  return {
    error: errorMessage,
  }
}

const CollectionSearchLoadingContainer = withRouter(
  connect(mapStateToProps)(LoadingBar)
)

export default CollectionSearchLoadingContainer
