import {connect} from 'react-redux'
import {withRouter} from 'react-router'
import {
  granuleToggleExcludeGlobal,
  granuleToggleFacet,
  granuleUpdateDateRange,
  granuleRemoveGeometry,
} from '../../../actions/routing/GranuleSearchStateActions'
import {asyncNewGranuleSearch} from '../../../actions/routing/GranuleSearchRouteActions'
import AppliedFilters from '../AppliedFilters'

const mapStateToProps = state => {
  const {
    selectedFacets,
    startDateTime,
    endDateTime,
    geoJSON,
    excludeGlobal,
  } = state.search.granuleFilter
  return {
    selectedFacets,
    startDateTime,
    endDateTime,
    geoJSON,
    excludeGlobal,
    showAppliedFilters: state.layout.showAppliedFilterBubbles,
  }
}

const mapDispatchToProps = (dispatch, ownProps) => {
  return {
    toggleExcludeGlobal: () => {
      dispatch(granuleToggleExcludeGlobal())
    },
    toggleFacet: (category, facetName, selected) =>
      dispatch(granuleToggleFacet(category, facetName, selected)),
    submit: () => {
      dispatch(
        asyncNewGranuleSearch(ownProps.history, ownProps.match.params.id)
      )
    },
    updateDateRange: (startDate, endDate) =>
      dispatch(granuleUpdateDateRange(startDate, endDate)),
    removeGeometry: () => dispatch(granuleRemoveGeometry()),
  }
}

const GranuleAppliedFiltersContainer = withRouter(
  connect(mapStateToProps, mapDispatchToProps)(AppliedFilters)
)

export default GranuleAppliedFiltersContainer
