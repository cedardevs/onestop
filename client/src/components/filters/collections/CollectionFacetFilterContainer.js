import {connect} from 'react-redux'
import {withRouter} from 'react-router'
import FacetFilter from '../facet/FacetFilter'
import {collectionToggleFacet} from '../../../actions/routing/CollectionSearchStateActions'
import {buildKeywordHierarchyMap} from '../../../utils/keywordUtils'
import {asyncNewCollectionSearch} from '../../../actions/routing/CollectionSearchRouteActions'

const mapStateToProps = state => {
  return {
    facets: buildKeywordHierarchyMap(
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
      dispatch(asyncNewCollectionSearch(ownProps.history))
    },
  }
}

const CollectionFacetFilterContainer = withRouter(
  connect(mapStateToProps, mapDispatchToProps)(FacetFilter)
)

export default CollectionFacetFilterContainer
