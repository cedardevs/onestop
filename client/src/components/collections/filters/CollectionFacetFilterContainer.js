import {connect} from 'react-redux'
import FacetFilter from '../../common/filters/facet/FacetFilter'
import {toggleFacet} from '../../../actions/SearchParamActions'
import {buildKeywordHierarchyMap} from '../../../utils/keywordUtils'
import {showCollections} from '../../../actions/FlowActions'

import {
  clearCollections,
  triggerSearch,
} from '../../../actions/SearchRequestActions'

import {withRouter} from 'react-router'

const mapStateToProps = state => {
  return {
    facets: buildKeywordHierarchyMap(
      state.domain.results.facets,
      state.behavior.search.selectedFacets
    ),
  }
}

const mapDispatchToProps = (dispatch, ownProps) => {
  return {
    toggleFacet: (category, facetName, selected) => {
      dispatch(toggleFacet(category, facetName, selected))
    },
    submit: () => {
      dispatch(clearCollections())
      dispatch(triggerSearch())
      dispatch(showCollections(ownProps.history))
    },
  }
}

const CollectionFacetFilterContainer = withRouter(
  connect(mapStateToProps, mapDispatchToProps)(FacetFilter)
)

export default CollectionFacetFilterContainer
