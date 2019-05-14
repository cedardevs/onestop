import {connect} from 'react-redux'
import {withRouter} from 'react-router'
import FacetFilter from '../facet/FacetFilter'
import {granuleToggleFacet} from '../../../actions/routing/GranuleSearchStateActions'
import {buildKeywordHierarchyMap} from '../../../utils/keywordUtils'
import {submitGranuleSearch} from '../../../actions/routing/GranuleSearchRouteActions'

const mapStateToProps = state => {
  return {
    facets: buildKeywordHierarchyMap(
      state.search.granuleResult.facets,
      state.search.granuleFilter.selectedFacets
    ),
  }
}

const mapDispatchToProps = (dispatch, ownProps) => {
  return {
    toggleFacet: (category, facetName, selected) => {
      dispatch(granuleToggleFacet(category, facetName, selected))
    },
    submit: () => {
      dispatch(
        submitGranuleSearch(ownProps.history, ownProps.match.params.id)
      )
    },
  }
}

const GranuleFacetFilterContainer = withRouter(
  connect(mapStateToProps, mapDispatchToProps)(FacetFilter)
)

export default GranuleFacetFilterContainer
