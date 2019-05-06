// synchronous actions
import {updateSelectedFacets} from '../../utils/filterUtils'

// Collection High-Level Filter Actions
// export const COLLECTION_UPDATE_FILTERS = 'COLLECTION_UPDATE_FILTERS'
// export const collectionUpdateFilters = filters => {
//   return {
//     type: COLLECTION_UPDATE_FILTERS,
//     filters: filters,
//   }
// }
//
// export const COLLECTION_REMOVE_FILTERS = 'COLLECTION_REMOVE_FILTERS'
// export const collectionRemoveFilters = () => {
//   return {
//     type: COLLECTION_REMOVE_FILTERS,
//   }
// }
//
// // Collection Query Text Actions
// export const COLLECTION_UPDATE_QUERY_TEXT = 'COLLECTION_UPDATE_QUERY_TEXT'
// export const collectionUpdateQueryText = queryText => {
//   return {
//     type: COLLECTION_UPDATE_QUERY_TEXT,
//     queryText: queryText,
//   }
// }
//
// // Collection Location Filter Actions
// export const COLLECTION_UPDATE_GEOMETRY = 'COLLECTION_UPDATE_GEOMETRY'
// export const collectionUpdateGeometry = geoJSON => {
//   return {
//     type: COLLECTION_UPDATE_GEOMETRY,
//     geoJSON: geoJSON,
//   }
// }
// export const COLLECTION_REMOVE_GEOMETRY = 'COLLECTION_REMOVE_GEOMETRY'
// export const collectionRemoveGeometry = () => {
//   return {
//     type: COLLECTION_REMOVE_GEOMETRY,
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

// Selected Collection IDs Filter Actions
// export const COLLECTION_TOGGLE_SELECTED_ID = 'COLLECTION_TOGGLE_SELECTED_ID'
// export const collectionToggleSelectedId = collectionId => {
//   return {
//     type: COLLECTION_TOGGLE_SELECTED_ID,
//     collectionId: collectionId,
//   }
// }
//
// export const COLLECTION_CLEAR_SELECTED_IDS = 'COLLECTION_CLEAR_SELECTED_IDS'
// export const collectionClearSelectedIds = () => {
//   return {
//     type: COLLECTION_CLEAR_SELECTED_IDS,
//   }
// }
//
// // Collection Keyword Filter Actions
// export const COLLECTION_TOGGLE_FACET = 'COLLECTION_TOGGLE_FACET'
// export const collectionToggleFacet = (category, facetName, selected) => {
//   return (dispatch, getState) => {
//     const {selectedFacets} = getState().search.collectionFilter
//     const newSelectedFacets = updateSelectedFacets(
//       selectedFacets,
//       category,
//       facetName,
//       selected
//     )
//     dispatch({
//       type: COLLECTION_TOGGLE_FACET,
//       selectedFacets: newSelectedFacets,
//     })
//   }
// }
export const GRANULE_CLEAR_FACETS = 'GRANULE_CLEAR_FACETS'
export const granuleClearFacets = () => {
  return {
    type: GRANULE_CLEAR_FACETS,
  }
}
//
// // Collection Exclude Global (Geometry) Results Filter
// export const COLLECTION_TOGGLE_EXCLUDE_GLOBAL =
//   'COLLECTION_TOGGLE_EXCLUDE_GLOBAL'
// export const collectionToggleExcludeGlobal = () => {
//   return {
//     type: COLLECTION_TOGGLE_EXCLUDE_GLOBAL,
//   }
// }
