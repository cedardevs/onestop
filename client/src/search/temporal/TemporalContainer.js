import { connect } from 'react-redux'
import { startDate, endDate, DateRange } from './TemporalActions'
import TemporalSearch from './TemporalSearchComponent'
import moment from 'moment'

const mapStateToProps = (state) => {
  const { startDateTime, endDateTime } = state.get('temporal').toJS()
  return {
    currentDate: state.getIn(['search', 'datetime']),
    startDateTime: state.getIn(['temporal', 'startDateTime']),
    endDateTime: state.getIn(['temporal', 'endDateTime']),
    userFriendlyStartDateTime: startDateTime ? moment(startDateTime).format('L') : '',
    userFriendlyEndDateTime: endDateTime ? moment(endDateTime).format('L') : ''
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
