import {connect} from 'react-redux'
import Detail from './Detail'

import {withRouter} from 'react-router'
import {showGranulesList} from '../../../actions/FlowActions'

const mapStateToProps = state => {
  const focusedItem = state.domain.results.collectionDetail
  return {
    id: focusedItem ? focusedItem.collection.id : null,
    item: focusedItem ? focusedItem.collection.attributes : null,
    totalGranuleCount: focusedItem ? focusedItem.totalGranuleCount : null,
    loading: state.behavior.request.getCollectionInFlight,
  }
}

const mapDispatchToProps = (dispatch, ownProps) => {
  return {
    navigateToGranules: id => {
      dispatch(showGranulesList(ownProps.history, id))
    },
  }
}

const DetailContainer = withRouter(
  connect(mapStateToProps, mapDispatchToProps)(Detail)
)

export default DetailContainer
