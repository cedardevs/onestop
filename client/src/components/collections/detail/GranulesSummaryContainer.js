import {connect} from 'react-redux'
import {withRouter} from 'react-router'
import GranulesSummary from './GranulesSummary'
import {submitGranuleSearchWithFilter} from '../../../actions/routing/GranuleSearchRouteActions'

const mapStateToProps = state => {
  const focusedItem = state.search.collectionDetailResult.collection
  const totalGranuleCount =
    state.search.collectionDetailResult.totalGranuleCount
  const totalGranuleFilteredCount =
    state.search.collectionDetailResult.filteredGranuleCount

  return {
    id: focusedItem ? focusedItem.id : null,
    totalGranuleCount: focusedItem ? totalGranuleCount : null,
    totalGranuleFilteredCount: focusedItem ? totalGranuleFilteredCount : null,
    loading: state.search.collectionDetailRequest.backgroundInFlight,
    filters: state.search.collectionDetailFilter, // set only to submit granule search on link
  }
}

const mapDispatchToProps = (dispatch, ownProps) => {
  return {
    navigateToGranules: (id, filterState) => {
      dispatch(submitGranuleSearchWithFilter(ownProps.history, id, filterState))
    },
  }
}

const GranulesSummaryContainer = withRouter(
  connect(mapStateToProps, mapDispatchToProps)(GranulesSummary)
)

export default GranulesSummaryContainer
