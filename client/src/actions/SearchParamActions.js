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
    const {selectedFacets} = getState().behavior.search
    const newSelectedFacets = updateSelectedFacets(selectedFacets, category,
        facetName, selected)
    dispatch({
      type: TOGGLE_FACET,
      selectedFacets: newSelectedFacets
    })
  }
}

export const TOGGLE_EXCLUDE_GLOBAL = 'TOGGLE_EXCLUDE_GLOBAL'
export const toggleExcludeGlobal = () => {
  return (dispatch, getState) => {
    dispatch({
      type: TOGGLE_EXCLUDE_GLOBAL
    })
  }
}

const updateSelectedFacets = (selectedFacets, category, term, selected) => {

  const selectedTerms = selectedFacets[category]

  // add to selected facets, if needed
  if (selected) {
    if (!selectedTerms) {
      // both category and term aren't yet in the selectedTerms
      return Immutable.set(selectedFacets, category, [term])
    }
    else if (!selectedTerms.includes(term)) {
      // the term isn't yet in the selectedTerms
      return Immutable.set(selectedFacets, category, selectedTerms.concat([term]))
    }
    else {
      // already selected, no need to duplicate term
      return selectedFacets
    }
  }
  // remove from selected facets, if needed
  else {
    if (!selectedTerms) {
      // can't remove if category doesn't exist in selectedFacets
      return selectedFacets
    }
    else {
      // search for index of term to be removed from selectedFacets
      let removeIndex = selectedTerms.indexOf(term)
      // the term exists to be removed
      if (removeIndex > -1) {
        const beforeRemove = selectedTerms.slice(0, removeIndex)
        const afterRemove = selectedTerms.slice(removeIndex + 1)
        const newTerms = beforeRemove.concat(afterRemove)

        // remove the whole category from selectedFacets if the new terms array is empty
        if (_.isEmpty(newTerms)) {
          return Immutable.without(selectedFacets, category)
        }
        // otherwise replace the category terms array with the newTerms
        else {
          return Immutable.set(selectedFacets, category, newTerms)
        }
      }
      // the term does not exist to be removed
      else {
        return selectedFacets
      }
    }
  }
}
