import { connect } from 'react-redux'
import { startDate, endDate, DateRange } from './TemporalActions'
import TemporalSearch from './TemporalComponent'

const mapStateToProps = (state) => {
  const { startDateTime, endDateTime } = state.get('temporal').toJS()
  return {
    startDateTime: state.getIn(['temporal', 'startDateTime']),
    endDateTime: state.getIn(['temporal', 'endDateTime'])
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
