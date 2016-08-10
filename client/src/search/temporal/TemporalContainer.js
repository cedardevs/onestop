import { connect } from 'react-redux'
import { startDate, endDate, DateRange } from './TemporalActions'
import TemporalSearch from './TemporalSearchComponent'

const mapStateToProps = (state) => {
  return {
    currentDate: state.getIn(['search', 'datetime'])
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
