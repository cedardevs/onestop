import {connect} from 'react-redux'
import {showDetails} from '../../actions/FlowActions'
import {
  incrementCollectionsOffset,
  triggerSearch,
} from '../../actions/SearchRequestActions'
import Collections from './Collections'

const mapStateToProps = state => {
  const {collections, totalCollections, pageSize} = state.domain.results
  return {
    loading: state.ui.loading ? 1 : 0,
    results: collections,
    totalHits: totalCollections,
    returnedHits: (collections && Object.keys(collections).length) || 0,
    pageSize,
  }
}

const mapDispatchToProps = dispatch => {
  return {
    selectCollection: id => {
      dispatch(showDetails(id))
    },
    fetchMoreResults: () => {
      dispatch(incrementCollectionsOffset())
      dispatch(triggerSearch(false))
    },
  }
}

const CollectionsContainer = connect(mapStateToProps, mapDispatchToProps)(
  Collections
)

export default CollectionsContainer
