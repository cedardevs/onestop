import {connect} from 'react-redux'
import TimeFilter from '../time/TimeFilter'
import {
  removeDateRange,
  updateDateRange,
} from '../../../actions/search/collections/SearchParamActions'
import {
  clearCollections,
  triggerSearch,
} from '../../../actions/search/collections/SearchRequestActions'
import {showCollections} from '../../../actions/search/collections/FlowActions'

import {withRouter} from 'react-router'

const mapStateToProps = state => {
  const {startDateTime, endDateTime} = state.behavior.search
  return {
    startDateTime: startDateTime,
    endDateTime: endDateTime,
  }
}

const mapDispatchToProps = (dispatch, ownProps) => {
  return {
    updateDateRange: (startDate, endDate) => {
      dispatch(updateDateRange(startDate, endDate))
    },
    removeDateRange: () => {
      dispatch(removeDateRange())
    },
    submit: () => {
      dispatch(clearCollections())
      dispatch(triggerSearch())
      dispatch(showCollections(ownProps.history))
    },
  }
}

const CollectionTimeFilterContainer = withRouter(
  connect(mapStateToProps, mapDispatchToProps)(TimeFilter)
)

export default CollectionTimeFilterContainer
