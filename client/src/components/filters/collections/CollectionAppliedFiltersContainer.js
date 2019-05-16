import {connect} from 'react-redux'
import {withRouter} from 'react-router'
import {
  collectionToggleExcludeGlobal,
  collectionToggleFacet,
  collectionUpdateDateRange,
  collectionRemoveGeometry,
} from '../../../actions/search/CollectionFilterActions'
import {collectionClearResults} from '../../../actions/search/CollectionResultActions'
import {
  triggerSearch,
  showCollections,
} from '../../../actions/search/SearchActions'
import CollectionAppliedFilters from './CollectionAppliedFilters'

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
    collectionToggleFacet: (category, facetName, selected) =>
      dispatch(collectionToggleFacet(category, facetName, selected)),
    submit: () => {
      dispatch(collectionClearResults())
      dispatch(triggerSearch())
      dispatch(showCollections(ownProps.history))
    },
    updateDateRange: (startDate, endDate) =>
      dispatch(collectionUpdateDateRange(startDate, endDate)),
    removeGeometry: () => dispatch(collectionRemoveGeometry()),
  }
}

const CollectionAppliedFiltersContainer = withRouter(
  connect(mapStateToProps, mapDispatchToProps)(CollectionAppliedFilters)
)

export default CollectionAppliedFiltersContainer
