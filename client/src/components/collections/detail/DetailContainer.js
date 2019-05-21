import {connect} from 'react-redux'
import {withRouter} from 'react-router'
import Detail from './Detail'

const mapStateToProps = state => {
  const focusedItem = state.search.collectionDetailResult.collection

  return {
    id: focusedItem ? focusedItem.id : null,
    item: focusedItem ? focusedItem.attributes : null,
    loading: state.search.collectionDetailRequest.inFlight,
  }
}

const mapDispatchToProps = (dispatch, ownProps) => {
  return {}
}

const DetailContainer = withRouter(
  connect(mapStateToProps, mapDispatchToProps)(Detail)
)

export default DetailContainer
