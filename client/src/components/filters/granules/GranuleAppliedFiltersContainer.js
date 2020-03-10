import {connect} from 'react-redux'
import {withRouter} from 'react-router'
import {
  granuleToggleExcludeGlobal,
  granuleToggleFacet,
  granuleUpdateDateRange,
  granuleUpdateYearRange,
  granuleRemoveGeometry,
  clearGranuleQueryText,
  granuleUpdateTimeRelation,
  granuleUpdateGeoRelation,
} from '../../../actions/routing/GranuleSearchStateActions'

import {submitGranuleSearch} from '../../../actions/routing/GranuleSearchRouteActions'
import AppliedFilters from '../AppliedFilters'

const mapStateToProps = state => {
  const {
    selectedFacets,
    startDateTime,
    endDateTime,
    startYear,
    endYear,
    bbox,
    excludeGlobal,
    title,
    allTermsMustMatch,
    geoRelationship,
    timeRelationship,
  } = state.search.granuleFilter
  return {
    selectedFacets,
    startDateTime,
    endDateTime,
    startYear,
    endYear,
    bbox,
    excludeGlobal,
    allTermsMustMatch,
    geoRelationship,
    timeRelationship,
    showAppliedFilters: state.layout.showAppliedFilterBubbles,
    textFilter: title,
  }
}

const mapDispatchToProps = (dispatch, ownProps) => {
  return {
    clearFilterText: () => {
      dispatch(clearGranuleQueryText())
    },
    toggleExcludeGlobal: () => {
      dispatch(granuleToggleExcludeGlobal())
    },
    toggleFacet: (category, facetName, selected) =>
      dispatch(granuleToggleFacet(category, facetName, selected)),
    submit: () => {
      dispatch(submitGranuleSearch(ownProps.history, ownProps.match.params.id))
    },
    updateDateRange: (startDate, endDate) =>
      dispatch(granuleUpdateDateRange(startDate, endDate)),
    updateYearRange: (startYear, endYear) =>
      dispatch(granuleUpdateYearRange(startYear, endYear)),
    removeGeometry: () => dispatch(granuleRemoveGeometry()),
    // updateTimeRelation: relation =>
    //   dispatch(granuleUpdateTimeRelation(relation)),
    // updateGeoRelation: relation => dispatch(granuleUpdateGeoRelation(relation)),
  }
}

const GranuleAppliedFiltersContainer = withRouter(
  connect(mapStateToProps, mapDispatchToProps)(AppliedFilters)
)

export default GranuleAppliedFiltersContainer
