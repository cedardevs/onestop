import { connect } from 'react-redux'
import FacetList from './FacetListComponent'
import { triggerSearch } from '../SearchActions'
import { toggleFacet } from './FacetActions'
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
