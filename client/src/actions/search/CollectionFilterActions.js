// synchronous actions
import {updateSelectedFacets} from '../../utils/filterUtils'

// Collection High-Level Filter Actions
export const COLLECTION_UPDATE_FILTERS = 'COLLECTION_UPDATE_FILTERS' // TODO rename setFiltersFromURL or something
export const collectionUpdateFilters = filters => {
  return {
    type: COLLECTION_UPDATE_FILTERS,
    filters: filters,
  }
}
export const COLLECTION_CLEAR_FILTERS = 'COLLECTION_CLEAR_FILTERS'
export const collectionClearFilters = () => {
  return {
    type: COLLECTION_CLEAR_FILTERS,
  }
}

// Collection Query Text Actions
export const COLLECTION_UPDATE_QUERY_TEXT = 'COLLECTION_UPDATE_QUERY_TEXT'
export const collectionUpdateQueryText = queryText => {
  return {
    type: COLLECTION_UPDATE_QUERY_TEXT,
    queryText: queryText,
  }
}

// Collection Location Filter Actions
export const COLLECTION_UPDATE_GEOMETRY = 'COLLECTION_UPDATE_GEOMETRY'
export const collectionUpdateGeometry = geoJSON => {
  return {
    type: COLLECTION_UPDATE_GEOMETRY,
    geoJSON: geoJSON,
  }
}
export const COLLECTION_REMOVE_GEOMETRY = 'COLLECTION_REMOVE_GEOMETRY'
export const collectionRemoveGeometry = () => {
  return {
    type: COLLECTION_REMOVE_GEOMETRY,
  }
}

// Collection Date Filter Actions
export const COLLECTION_UPDATE_DATE_RANGE = 'COLLECTION_UPDATE_DATE_RANGE'
export const collectionUpdateDateRange = (startDate, endDate) => {
  return {
    type: COLLECTION_UPDATE_DATE_RANGE,
    startDate: startDate,
    endDate: endDate,
  }
}
export const COLLECTION_REMOVE_DATE_RANGE = 'COLLECTION_REMOVE_DATE_RANGE'
export const collectionRemoveDateRange = () => {
  return {
    type: COLLECTION_REMOVE_DATE_RANGE,
  }
}

// Collection Keyword Filter Actions
export const COLLECTION_TOGGLE_FACET = 'COLLECTION_TOGGLE_FACET'
export const collectionToggleFacet = (category, facetName, selected) => {
  return (dispatch, getState) => {
    const {selectedFacets} = getState().search.collectionFilter
    const newSelectedFacets = updateSelectedFacets(
      selectedFacets,
      category,
      facetName,
      selected
    )
    dispatch({
      type: COLLECTION_TOGGLE_FACET,
      selectedFacets: newSelectedFacets,
    })
  }
}

// Collection Exclude Global (Geometry) Results Filter
export const COLLECTION_TOGGLE_EXCLUDE_GLOBAL =
  'COLLECTION_TOGGLE_EXCLUDE_GLOBAL'
export const collectionToggleExcludeGlobal = () => {
  return {
    type: COLLECTION_TOGGLE_EXCLUDE_GLOBAL,
  }
}
