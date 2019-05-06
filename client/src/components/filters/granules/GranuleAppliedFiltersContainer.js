import {connect} from 'react-redux'
import {withRouter} from 'react-router'
import {
  // collectionToggleExcludeGlobal,
  // collectionToggleFacet,
  granuleUpdateDateRange,
  // collectionRemoveGeometry,
} from '../../../actions/search/GranuleFilterActions'
import {granuleClearResults} from '../../../actions/search/GranuleResultActions'
import {
  triggerGranuleSearch,
  showGranules,
} from '../../../actions/search/GranuleSearchActions'
import AppliedFilters from '../AppliedFilters'

const mapStateToProps = state => {
  // const focusedItem = state.search.collectionResult.collectionDetail
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
    // collectionId: focusedItem ? focusedItem.collection.id : null,
  }
}

const mapDispatchToProps = (dispatch, ownProps) => {
  return {
    // toggleExcludeGlobal: () => {
    //   dispatch(collectionToggleExcludeGlobal())
    // },
    // toggleFacet: (category, facetName, selected) =>
    //   dispatch(collectionToggleFacet(category, facetName, selected)),
    submit: () => {
      // dispatch(collectionClearResults())

      dispatch(triggerGranuleSearch())
      dispatch(showGranules(ownProps.history, ownProps.match.params.id))
    },
    updateDateRange: (startDate, endDate) =>
      dispatch(granuleUpdateDateRange(startDate, endDate)),
    // removeGeometry: () => dispatch(collectionRemoveGeometry()),
  }
}

const CollectionAppliedFiltersContainer = withRouter(
  connect(mapStateToProps, mapDispatchToProps)(AppliedFilters)
)

export default CollectionAppliedFiltersContainer
