import Immutable from 'seamless-immutable'
import _ from 'lodash'

export const UPDATE_QUERY = 'update_query'
export const updateQuery = (searchText) => {
  return {
    type: UPDATE_QUERY,
    searchText
  }
}

export const UPDATE_SEARCH = 'update_search'
export const updateSearch = (params) => {
  return {
    type: UPDATE_SEARCH,
    params
  }
}

export const UPDATE_DATE_RANGE = 'UPDATE_DATE_RANGE'
export const updateDateRange = (startDate, endDate) => {
  return {
    type: UPDATE_DATE_RANGE,
    startDate,
    endDate
  }
}

export const NEW_GEOMETRY = 'new_geometry'
export const newGeometry = (geoJSON) => {
  return {
    type: NEW_GEOMETRY,
    geoJSON
  }
}

export const REMOVE_GEOMETRY = 'remove_geometry'
export const removeGeometry = () => {
  return {
    type: REMOVE_GEOMETRY
  }
}

export const TOGGLE_SELECTION = 'toggle_selection'
export const toggleSelection = (collectionId) => {
  return {
    type: TOGGLE_SELECTION,
    id: collectionId
  }
}

export const CLEAR_SELECTIONS = 'clear_selections'
export const clearSelections = () => {
  return {
    type: CLEAR_SELECTIONS
  }
}

export const TOGGLE_FACET = 'TOGGLE_FACET'
export const toggleFacet = (category, facetName, selected) => {
  return (dispatch, getState) => {
    const { selectedFacets } = getState().behavior.search
    const newSelectedFacets = updateSelectedFacets(selectedFacets, category,
        facetName, selected)
    dispatch({
      type: TOGGLE_FACET,
      selectedFacets: newSelectedFacets
    })
  }
}

const updateSelectedFacets = (selectedFacets, category, facetName, selected ) => {
  if (selected) {
    const newList = selectedFacets[category]
        && selectedFacets[category].concat([facetName])
        || [facetName]
    return Immutable.set(selectedFacets, category, newList)
  } else {
    let idx = selectedFacets[category].indexOf(facetName)
    let newList = selectedFacets[category]
        .slice(0, idx)
        .concat(selectedFacets[category].slice(idx + 1))
    return !_.isEmpty(newList) ? Immutable.set(selectedFacets, category, newList)
        : Immutable.without(selectedFacets, category)
  }
}
