import {connect} from 'react-redux'
import {
  toggleExcludeGlobal,
  toggleFacet,
  updateDateRange,
  removeGeometry,
} from '../../../actions/SearchParamActions'
import {
  clearCollections,
  triggerSearch,
} from '../../../actions/SearchRequestActions'
import {showCollections} from '../../../actions/FlowActions'
import CollectionAppliedFilters from './CollectionAppliedFilters'

import {withRouter} from 'react-router'

const mapStateToProps = state => {
  const {
    selectedFacets,
    startDateTime,
    endDateTime,
    geoJSON,
    excludeGlobal,
  } = state.behavior.search
  return {
    selectedFacets,
    startDateTime,
    endDateTime,
    geoJSON,
    excludeGlobal,
    showAppliedFilters: state.ui.layout.showAppliedFilterBubbles,
  }
}

const mapDispatchToProps = (dispatch, ownProps) => {
  return {
    toggleExcludeGlobal: () => {
      dispatch(toggleExcludeGlobal())
    },
    toggleFacet: (category, facetName, selected) =>
      dispatch(toggleFacet(category, facetName, selected)),
    submit: () => {
      dispatch(clearCollections())
      dispatch(triggerSearch())
      dispatch(showCollections(ownProps.history))
    },
    updateDateRange: (startDate, endDate) =>
      dispatch(updateDateRange(startDate, endDate)),
    removeGeometry: () => dispatch(removeGeometry()),
  }
}

const CollectionAppliedFiltersContainer = withRouter(
  connect(mapStateToProps, mapDispatchToProps)(CollectionAppliedFilters)
)

export default CollectionAppliedFiltersContainer
