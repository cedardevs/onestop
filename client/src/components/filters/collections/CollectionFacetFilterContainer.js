import {connect} from 'react-redux'
import {withRouter} from 'react-router'
import FacetFilter from '../facet/FacetFilter'
import {collectionToggleFacet} from '../../../actions/search/CollectionFilterActions'
import {buildKeywordHierarchyMap} from '../../../utils/keywordUtils'
import {
  triggerCollectionSearch,
  showCollections,
} from '../../../actions/search/CollectionSearchActions'
import {collectionClearResults} from '../../../actions/search/CollectionResultActions'

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
      dispatch(collectionClearResults())
      dispatch(triggerCollectionSearch())
      dispatch(showCollections(ownProps.history))
    },
  }
}

const CollectionFacetFilterContainer = withRouter(
  connect(mapStateToProps, mapDispatchToProps)(FacetFilter)
)

export default CollectionFacetFilterContainer
