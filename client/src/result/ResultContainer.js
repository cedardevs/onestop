import { connect } from 'react-redux'
// import ResultLayout from './ResultLayout'
import Result from './Result'
import { toggleFacet, updateDateRange } from '../actions/SearchParamActions'
import { clearCollections, triggerSearch } from '../actions/SearchRequestActions'
import { showCollections } from '../actions/FlowActions'

const mapStateToProps = (state) => {
  const {selectedFacets, startDateTime, endDateTime} = state.behavior.search
  return {
    selectedFacets,
    startDateTime,
    endDateTime
  }
}

const mapDispatchToProps = (dispatch) => {
  return {
    toggleFacet: (category, facetName, selected) =>
        dispatch(toggleFacet(category, facetName, selected)),
    submit: () => {
      dispatch(clearCollections())
      dispatch(triggerSearch())
      dispatch(showCollections())
    },
    updateDateRange: (startDate, endDate) =>
        dispatch(updateDateRange(startDate, endDate))
  }
}

const ResultContainer = connect(
    mapStateToProps,
    mapDispatchToProps
)(Result)

export default ResultContainer
