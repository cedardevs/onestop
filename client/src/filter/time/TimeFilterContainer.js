import { connect } from 'react-redux'
import TimeFilter from "./TimeFilter";
import {removeDateRange, updateDateRange} from "../../actions/SearchParamActions"
import {
  clearCollections,
  triggerSearch,
} from '../../actions/SearchRequestActions'
import { showCollections } from '../../actions/FlowActions'

const mapStateToProps = state => {
  const {startDateTime, endDateTime} = state.behavior.search
  return {
    startDateTime: startDateTime,
    endDateTime: endDateTime
  }
}

const mapDispatchToProps = dispatch => {
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
      dispatch(showCollections())
    }
  }
}

const TimeFilterContainer = connect(mapStateToProps, mapDispatchToProps)(
  TimeFilter
)

export default TimeFilterContainer