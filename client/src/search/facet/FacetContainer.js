import { connect } from 'react-redux'
import FacetList from './FacetListComponent'
import { triggerSearch, updateQuery } from '../SearchActions'
import { toggleFacet } from './FacetActions'

const mapStateToProps = (state) => {
  return {
    facetMap: state.facets.allFacets,
    selectedFacets: state.facets.selectedFacets
  }
}

const mapDispatchToProps = (dispatch) => {
  return {
    toggleFacet: (category, facetName, selected) =>
      dispatch(toggleFacet(category, facetName, selected)),
    submit: () => dispatch(triggerSearch())
  }
}

const FacetContainer = connect(
  mapStateToProps,
  mapDispatchToProps
)(FacetList)

export default FacetContainer
