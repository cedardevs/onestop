import { connect } from 'react-redux'
import { startDate, endDate, DateRange } from './TemporalActions'
import TemporalSearch from './TemporalSearchComponent'
import moment from 'moment'

const mapStateToProps = (state) => {
  const { startDateTime, endDateTime } = state.temporal
  return {
    startDateTime: state.temporal.startDateTime,
    endDateTime: state.temporal.endDateTime
  }
}

const mapDispatchToProps = (dispatch) => {
  return {
    updateOnChange: (text, dateSelected) => {
      if (dateSelected === DateRange.START_DATE){
        dispatch(startDate(text))
      } else {
        dispatch(endDate(text))
      }
    }
  }
}

const TemporalContainer = connect(
    mapStateToProps,
    mapDispatchToProps
)(TemporalSearch)

export default TemporalContainer
