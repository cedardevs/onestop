import {connect} from 'react-redux'
import {withRouter} from 'react-router'
import TimeFilter from '../time/TimeFilter'
import {
  collectionRemoveDateRange,
  collectionUpdateDateRange,
  collectionUpdateYearRange,
  collectionRemoveYearRange,
} from '../../../actions/routing/CollectionSearchStateActions'
import {submitCollectionSearch} from '../../../actions/routing/CollectionSearchRouteActions'

const mapStateToProps = state => {
  const {
    startDateTime,
    endDateTime,
    startYear,
    endYear,
  } = state.search.collectionFilter
  return {
    startDateTime,
    endDateTime,
    startYear,
    endYear,
  }
}

const mapDispatchToProps = (dispatch, ownProps) => {
  return {
    updateDateRange: (startDate, endDate) => {
      dispatch(collectionUpdateDateRange(startDate, endDate))
    },
    updateYearRange: (startYear, endYear) => {
      dispatch(collectionUpdateYearRange(startYear, endYear))
    },
    removeDateRange: () => {
      dispatch(collectionRemoveDateRange())
    },
    removeYearRange: () => {
      dispatch(collectionRemoveYearRange())
    },
    submit: () => {
      dispatch(submitCollectionSearch(ownProps.history))
    },
  }
}

const CollectionTimeFilterContainer = withRouter(
  connect(mapStateToProps, mapDispatchToProps)(TimeFilter)
)

export default CollectionTimeFilterContainer
