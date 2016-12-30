import { connect } from 'react-redux'
import LandingComponent from './LandingComponent'
import { triggerSearch, updateQuery } from '../search/SearchActions'
import { clearFacets } from '../search/facet/FacetActions'

const mapStateToProps = (state) => {
  return {
    queryString: state.search.text
  }
}

const mapDispatchToProps = (dispatch) => {
  return {
    submit: () => {
      dispatch(clearFacets())
      dispatch(triggerSearch())
    },
    updateQuery: (text) => dispatch(updateQuery(text))
  }
}

const LandingContainer = connect(
    mapStateToProps,
    mapDispatchToProps
)(LandingComponent)

export default LandingContainer
