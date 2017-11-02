import { connect } from 'react-redux'
import { updateDateRange } from '../../actions/SearchParamActions'
import TemporalSearch from './TemporalSearch'

const mapStateToProps = (state) => {
  const { startDateTime, endDateTime } = state.behavior.search
  return {
    startDateTime: startDateTime,
    endDateTime: endDateTime
  }
}

const mapDispatchToProps = (dispatch) => {
  return {
    updateOnChange: (startDate, endDate) => {
      dispatch(updateDateRange(startDate, endDate))
    }
  }
}

const TemporalSearchContainer = connect(
    mapStateToProps,
    mapDispatchToProps
)(TemporalSearch)

export default TemporalSearchContainer
