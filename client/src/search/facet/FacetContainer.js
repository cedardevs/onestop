import { connect } from 'react-redux'
import FacetList from './FacetListComponent'
import { clearCollections, triggerSearch } from '../../actions/SearchRequestActions'
import { toggleFacet } from '../../actions/SearchParamActions'
import { showCollections } from '../../actions/FlowActions'

const mapStateToProps = (state) => {
  return {
    facetMap: state.domain.results.facets,
    selectedFacets: state.behavior.search.selectedFacets
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
    }
  }
}

const FacetContainer = connect(
  mapStateToProps,
  mapDispatchToProps
)(FacetList)

export default FacetContainer
