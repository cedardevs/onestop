import { connect } from 'react-redux'
import { updateDateRange } from '../../actions/SearchParamActions'
import TemporalSearch from './TemporalComponent'

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

const TemporalContainer = connect(
    mapStateToProps,
    mapDispatchToProps
)(TemporalSearch)

export default TemporalContainer
