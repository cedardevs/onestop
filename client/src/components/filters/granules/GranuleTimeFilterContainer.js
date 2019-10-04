import {connect} from 'react-redux'
import {withRouter} from 'react-router'
import TimeFilter from '../time/TimeFilter'
import {
  granuleRemoveDateRange,
  granuleUpdateDateRange,
  granuleUpdateYearRange,
} from '../../../actions/routing/GranuleSearchStateActions'
import {submitGranuleSearch} from '../../../actions/routing/GranuleSearchRouteActions'

const mapStateToProps = state => {
  const {
    startDateTime,
    endDateTime,
    startYear,
    endYear,
  } = state.search.granuleFilter
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
      dispatch(granuleUpdateDateRange(startDate, endDate))
    },
    updateYearRange: (startYear, endYear) => {
      dispatch(granuleUpdateYearRange(startYear, endYear))
    },
    removeDateRange: () => {
      dispatch(granuleRemoveDateRange())
    },
    removeYearRange: () => {
      dispatch(granuleRemoveYearRange())
    },
    submit: () => {
      dispatch(submitGranuleSearch(ownProps.history, ownProps.match.params.id))
    },
  }
}

const GranuleTimeFilterContainer = withRouter(
  connect(mapStateToProps, mapDispatchToProps)(TimeFilter)
)

export default GranuleTimeFilterContainer
