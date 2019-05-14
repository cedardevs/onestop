import {connect} from 'react-redux'
import {withRouter} from 'react-router'
import TimeFilter from '../time/TimeFilter'
import {
  collectionRemoveDateRange,
  collectionUpdateDateRange,
} from '../../../actions/routing/CollectionSearchStateActions'
import {asyncNewCollectionSearch} from '../../../actions/routing/CollectionSearchRouteActions'

const mapStateToProps = state => {
  const {startDateTime, endDateTime} = state.search.collectionFilter
  return {
    startDateTime: startDateTime,
    endDateTime: endDateTime,
  }
}

const mapDispatchToProps = (dispatch, ownProps) => {
  return {
    updateDateRange: (startDate, endDate) => {
      dispatch(collectionUpdateDateRange(startDate, endDate))
    },
    removeDateRange: () => {
      dispatch(collectionRemoveDateRange())
    },
    submit: () => {
      dispatch(asyncNewCollectionSearch(ownProps.history))
    },
  }
}

const CollectionTimeFilterContainer = withRouter(
  connect(mapStateToProps, mapDispatchToProps)(TimeFilter)
)

export default CollectionTimeFilterContainer
