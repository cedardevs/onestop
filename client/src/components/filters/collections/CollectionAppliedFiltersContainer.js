import {connect} from 'react-redux'
import {withRouter} from 'react-router'
import {
  collectionToggleExcludeGlobal,
  collectionToggleFacet,
  collectionUpdateDateRange,
  collectionRemoveGeometry,
} from '../../../actions/routing/CollectionSearchStateActions'
import {asyncNewCollectionSearch} from '../../../actions/routing/CollectionSearchRouteActions'
import AppliedFilters from '../AppliedFilters'

const mapStateToProps = state => {
  const {
    selectedFacets,
    startDateTime,
    endDateTime,
    geoJSON,
    excludeGlobal,
  } = state.search.collectionFilter
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
      dispatch(collectionToggleExcludeGlobal())
    },
    toggleFacet: (category, facetName, selected) =>
      dispatch(collectionToggleFacet(category, facetName, selected)),
    submit: () => {
      dispatch(asyncNewCollectionSearch(ownProps.history))
    },
    updateDateRange: (startDate, endDate) =>
      dispatch(collectionUpdateDateRange(startDate, endDate)),
    removeGeometry: () => dispatch(collectionRemoveGeometry()),
  }
}

const CollectionAppliedFiltersContainer = withRouter(
  connect(mapStateToProps, mapDispatchToProps)(AppliedFilters)
)

export default CollectionAppliedFiltersContainer
