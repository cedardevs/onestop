import { connect } from 'react-redux'
import FacetFilter from './FacetFilter'
import { toggleFacet } from '../actions/SearchParamActions'

const mapStateToProps = (state) => {
  return {
    facetMap: state.domain.results.facets,
    selectedFacets: state.behavior.search.selectedFacets
  }
}

const mapDispatchToProps = (dispatch) => {
  return {
    toggleFacet: (category, facetName, selected) =>
      dispatch(toggleFacet(category, facetName, selected))
  }
}

const FacetFilterContainer = connect(
  mapStateToProps,
  mapDispatchToProps
)(FacetFilter)

export default FacetFilterContainer