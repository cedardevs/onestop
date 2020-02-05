import {connect} from 'react-redux'
import LoadingBar from './LoadingBar'

import {withRouter} from 'react-router'

const mapStateToProps = state => {
  const {errorMessage} = state.search.collectionDetailRequest

  return {
    error: errorMessage,
  }
}

const CollectionGetDetailLoadingContainer = withRouter(
  connect(mapStateToProps)(LoadingBar)
)

export default CollectionGetDetailLoadingContainer
