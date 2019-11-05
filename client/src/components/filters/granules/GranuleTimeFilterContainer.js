import {connect} from 'react-redux'
import {withRouter} from 'react-router'
import TimeFilter from '../time/TimeFilter'
import {
  granuleRemoveDateRange,
  granuleUpdateDateRange,
  granuleUpdateYearRange,
  granuleRemoveYearRange,
  granuleUpdateTimeRelation,
} from '../../../actions/routing/GranuleSearchStateActions'
import {submitGranuleSearch} from '../../../actions/routing/GranuleSearchRouteActions'

const mapStateToProps = state => {
  const {
    startDateTime,
    endDateTime,
    startYear,
    endYear,
    timeRelationship,
  } = state.search.granuleFilter
  return {
    startDateTime,
    endDateTime,
    startYear,
    endYear,
    timeRelationship,
  }
}

const mapDispatchToProps = (dispatch, ownProps) => {
  return {
    updateTimeRelation: relation => {
      dispatch(granuleUpdateTimeRelation(relation))
    },
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
