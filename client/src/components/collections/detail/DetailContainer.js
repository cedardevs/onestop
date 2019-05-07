import {connect} from 'react-redux'
import {withRouter} from 'react-router'
import Detail from './Detail'
import {showGranules} from '../../../actions/search/GranuleSearchActions'

const mapStateToProps = state => {
  const focusedItem = state.search.collectionResult.collectionDetail
  return {
    id: focusedItem ? focusedItem.collection.id : null,
    item: focusedItem ? focusedItem.collection.attributes : null,
    totalGranuleCount: focusedItem ? focusedItem.totalGranuleCount : null,
    loading: state.search.collectionRequest.collectionDetailRequestInFlight,
  }
}

const mapDispatchToProps = (dispatch, ownProps) => {
  return {
    navigateToGranules: id => {
      dispatch(showGranules(ownProps.history, id))
    },
  }
}

const DetailContainer = withRouter(
  connect(mapStateToProps, mapDispatchToProps)(Detail)
)

export default DetailContainer
