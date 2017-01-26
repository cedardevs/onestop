import { connect } from 'react-redux'
import LandingComponent from './LandingComponent'
import { triggerSearch, updateQuery } from '../search/SearchActions'
import { clearFacets } from '../search/facet/FacetActions'
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

const LandingContainer = connect(
    mapStateToProps,
    mapDispatchToProps
)(LandingComponent)

export default LandingContainer
