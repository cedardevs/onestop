import { connect } from 'react-redux'
import { startDate, endDate, DateRange } from './TemporalActions'
import TemporalSearch from './TemporalSearchComponent'

const dateStateToProps = (state) => {
  return {
    currentDate: state.getIn(['search', 'datetime'])
  }
}

const dateDispatchToProps = (dispatch) => {
  return {
    onChange: (text, dateSelected) => {
      if (dateSelected === DateRange.START_DATE){
        dispatch(startDate(text))
      } else {
        dispatch(endDate(text))
      }
    }
  }
}

const TemporalContainer = connect(
    dateStateToProps,
    dateDispatchToProps
)(TemporalSearch)

export default TemporalContainer
