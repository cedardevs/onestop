import { connect } from 'react-redux'
import { temporalSearch } from './TemporalAction'
import TemporalSearch from './TemporalSearchComponent'

const dateStateToProps = (state) => {
  return {
    currentDate: state.getIn(['search', 'datetime'])
  }
}

const dateDispatchToProps = (dispatch) => {
  return {
    onChange: (text) => {
      console.log(text)
      dispatch(temporalSearch(text))
    }
  }
}

const TemporalContainer = connect(
    dateStateToProps,
    dateDispatchToProps
)(TemporalSearch)

export default TemporalContainer 
