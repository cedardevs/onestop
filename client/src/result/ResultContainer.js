import {connect} from 'react-redux'
import Result from './Result'
import {
  toggleExcludeGlobal,
  toggleFacet,
  updateDateRange,
  removeGeometry,
} from '../actions/SearchParamActions'
import {clearCollections, triggerSearch} from '../actions/SearchRequestActions'
import {showCollections} from '../actions/FlowActions'

const mapStateToProps = state => {
  const {
    selectedFacets,
    startDateTime,
    endDateTime,
    geoJSON,
    excludeGlobal,
  } = state.behavior.search
  return {
    selectedFacets,
    startDateTime,
    endDateTime,
    geoJSON,
    excludeGlobal,
  }
}

const mapDispatchToProps = dispatch => {
  return {
    toggleExcludeGlobal: () => {
      dispatch(toggleExcludeGlobal())
    },
    toggleFacet: (category, facetName, selected) =>
      dispatch(toggleFacet(category, facetName, selected)),
    submit: () => {
      dispatch(clearCollections())
      dispatch(triggerSearch())
      dispatch(showCollections())
    },
    updateDateRange: (startDate, endDate) =>
      dispatch(updateDateRange(startDate, endDate)),
    removeGeometry: () => dispatch(removeGeometry()),
  }
}

const ResultContainer = connect(mapStateToProps, mapDispatchToProps)(Result)

export default ResultContainer
