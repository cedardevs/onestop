// synchronous actions
import {updateSelectedFacets} from '../../utils/filterUtils'

// Granule High-Level Filter Actions
export const GRANULE_UPDATE_FILTERS = 'GRANULE_UPDATE_FILTERS'
export const granuleUpdateFilters = filters => {
  return {
    type: GRANULE_UPDATE_FILTERS,
    filters: filters,
  }
}
//
// export const GRANULE_REMOVE_FILTERS = 'GRANULE_REMOVE_FILTERS'
// export const granuleRemoveFilters = () => {
//   return {
//     type: GRANULE_REMOVE_FILTERS,
//   }
// }
//
// // Granule Query Text Actions
// export const GRANULE_UPDATE_QUERY_TEXT = 'GRANULE_UPDATE_QUERY_TEXT'
// export const granuleUpdateQueryText = queryText => {
//   return {
//     type: GRANULE_UPDATE_QUERY_TEXT,
//     queryText: queryText,
//   }
// }
//
// // Granule Location Filter Actions
// export const GRANULE_UPDATE_GEOMETRY = 'GRANULE_UPDATE_GEOMETRY'
// export const granuleUpdateGeometry = geoJSON => {
//   return {
//     type: GRANULE_UPDATE_GEOMETRY,
//     geoJSON: geoJSON,
//   }
// }
// export const GRANULE_REMOVE_GEOMETRY = 'GRANULE_REMOVE_GEOMETRY'
// export const granuleRemoveGeometry = () => {
//   return {
//     type: GRANULE_REMOVE_GEOMETRY,
//   }
// }

// Granule Date Filter Actions
export const GRANULE_UPDATE_DATE_RANGE = 'GRANULE_UPDATE_DATE_RANGE'
export const granuleUpdateDateRange = (startDate, endDate) => {
  return {
    type: GRANULE_UPDATE_DATE_RANGE,
    startDate: startDate,
    endDate: endDate,
  }
}
export const GRANULE_REMOVE_DATE_RANGE = 'GRANULE_REMOVE_DATE_RANGE'
export const granuleRemoveDateRange = () => {
  return {
    type: GRANULE_REMOVE_DATE_RANGE,
  }
}

// Selected Granule IDs Filter Actions
// export const GRANULE_TOGGLE_SELECTED_ID = 'GRANULE_TOGGLE_SELECTED_ID'
// export const granuleToggleSelectedId = granuleId => {
//   return {
//     type: GRANULE_TOGGLE_SELECTED_ID,
//     granuleId: granuleId,
//   }
// }
//
// export const GRANULE_CLEAR_SELECTED_IDS = 'GRANULE_CLEAR_SELECTED_IDS'
// export const granuleClearSelectedIds = () => {
//   return {
//     type: GRANULE_CLEAR_SELECTED_IDS,
//   }
// }
//
// // Granule Keyword Filter Actions
export const GRANULE_TOGGLE_FACET = 'GRANULE_TOGGLE_FACET' // TODO rename, it's not toggle facet (singular) so much as setFacets
export const granuleToggleFacet = (category, facetName, selected) => {
  return (dispatch, getState) => {
    const {selectedFacets} = getState().search.granuleFilter
    const newSelectedFacets = updateSelectedFacets(
      selectedFacets,
      category,
      facetName,
      selected
    )
    dispatch({
      type: GRANULE_TOGGLE_FACET,
      selectedFacets: newSelectedFacets,
    })
  }
}
// export const GRANULE_CLEAR_FACETS = 'GRANULE_CLEAR_FACETS'
// export const granuleClearFacets = () => {
//   return {
//     type: GRANULE_CLEAR_FACETS,
//   }
// }
//
// // Granule Exclude Global (Geometry) Results Filter
// export const GRANULE_TOGGLE_EXCLUDE_GLOBAL =
//   'GRANULE_TOGGLE_EXCLUDE_GLOBAL'
// export const granuleToggleExcludeGlobal = () => {
//   return {
//     type: GRANULE_TOGGLE_EXCLUDE_GLOBAL,
//   }
// }
