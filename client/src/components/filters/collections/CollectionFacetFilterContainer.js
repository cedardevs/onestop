import {connect} from 'react-redux'
import {withRouter} from 'react-router'
import FacetFilter from '../facet/FacetFilter'
import {collectionToggleFacet} from '../../../actions/routing/CollectionSearchStateActions'
import {buildFilterHierarchyMap} from '../../../utils/facetUtils'
import {submitCollectionSearch} from '../../../actions/routing/CollectionSearchRouteActions'

const mapStateToProps = state => {
  return {
    facets: buildFilterHierarchyMap(
      state.search.collectionResult.facets,
      state.search.collectionFilter.selectedFacets
    ),
  }
}

const mapDispatchToProps = (dispatch, ownProps) => {
  return {
    toggleFacet: (category, facetName, selected) => {
      dispatch(collectionToggleFacet(category, facetName, selected))
    },
    submit: () => {
      dispatch(submitCollectionSearch(ownProps.history))
    },
  }
}

const CollectionFacetFilterContainer = withRouter(
  connect(mapStateToProps, mapDispatchToProps)(FacetFilter)
)

export default CollectionFacetFilterContainer
