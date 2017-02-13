import { connect } from 'react-redux'
import Section508LandingComponent from './Section508LandingComponent'
import { triggerSearch, clearFacets } from '../actions/SearchRequestActions'
import { updateQuery } from '../actions/SearchParamActions'
import { showCollections } from '../actions/FlowActions'

const mapStateToProps = (state) => {
  return {
    queryString: state.behavior.search.queryText.text
  }
}

const mapDispatchToProps = (dispatch) => {
  return {
    submit: () => {
      dispatch(clearFacets())
      dispatch(triggerSearch())
      dispatch(showCollections())
    },
    updateQuery: (text) => dispatch(updateQuery(text))
  }
}

const Section508LandingContainer = connect(
    mapStateToProps,
    mapDispatchToProps
)(Section508LandingComponent)

export default Section508LandingContainer
