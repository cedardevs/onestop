import { connect } from 'react-redux'
import Section508Landing from './Section508Landing'
import { triggerSearch, clearFacets } from '../actions/SearchRequestActions'
import { showCollections } from '../actions/FlowActions'
import { updateSearch } from '../actions/SearchParamActions'

const mapStateToProps = (state) => {
  const {queryText, startDateTime, endDateTime, geoJSON} = state.behavior.search
  return {
    queryText: queryText,
    startDateTime: startDateTime,
    endDateTime: endDateTime,
    geoJSON: geoJSON
  }
}

const mapDispatchToProps = (dispatch) => {
  return {
    clearSearch: () => dispatch(updateSearch()),
    updateSearch: (params) => dispatch(updateSearch(params)),
    submit: () => {
      dispatch(clearFacets())
      dispatch(triggerSearch())
      dispatch(showCollections('508'))
    }
  }
}

const Section508LandingContainer = connect(
    mapStateToProps,
    mapDispatchToProps
)(Section508Landing)

export default Section508LandingContainer
