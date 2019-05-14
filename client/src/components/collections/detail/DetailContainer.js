import {connect} from 'react-redux'
import {withRouter} from 'react-router'
import Detail from './Detail'
import {asyncNewGranuleSearch} from '../../../actions/routing/GranuleSearchRouteActions'

const mapStateToProps = state => {
  const focusedItem = state.search.collectionDetailResult.collection
  const totalGranuleCount =
    state.search.collectionDetailResult.totalGranuleCount

  return {
    id: focusedItem ? focusedItem.id : null,
    item: focusedItem ? focusedItem.attributes : null,
    totalGranuleCount: focusedItem ? totalGranuleCount : null,
    loading: state.search.collectionDetailRequest.inFlight,
  }
}

const mapDispatchToProps = (dispatch, ownProps) => {
  return {
    navigateToGranules: id => {
      dispatch(asyncNewGranuleSearch(ownProps.history, id))
    },
  }
}

const DetailContainer = withRouter(
  connect(mapStateToProps, mapDispatchToProps)(Detail)
)

export default DetailContainer
