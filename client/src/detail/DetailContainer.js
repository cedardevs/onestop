import {connect} from 'react-redux'
import {showGranulesList} from '../actions/FlowActions'
import Detail from './Detail'

import {withRouter} from 'react-router'

const mapStateToProps = (state, reactProps) => {
  const focusedItem = state.domain.results.collectionDetail
  return {
    id: focusedItem ? focusedItem.collection.id : null,
    item: focusedItem ? focusedItem.collection.attributes : null,
    totalGranuleCount: focusedItem ? focusedItem.totalGranuleCount : null,
    loading: state.behavior.request.getCollectionInFlight,
  }
}

const mapDispatchToProps = dispatch => {
  return {
    navigateToGranules: id => {
      dispatch(showGranulesList(id))
    },
  }
}

const DetailContainer = withRouter(
  connect(mapStateToProps, mapDispatchToProps)(Detail)
)

export default DetailContainer
