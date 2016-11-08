export const TOGGLE_GRANULE_FOCUS = 'toggle_granule_focus'
export const TOGGLE_COLLECTION_SELECTION = 'toggle_collection_selection'
export const FETCHING_GRANULES = 'fetching_granules'
export const FETCHED_GRANULES = 'fetched_granules'
export const CLEAR_GRANULES = 'clear_granules'

export const toggleGranuleFocus = (granuleId) => {
  return {
    type: TOGGLE_GRANULE_FOCUS,
    id: granuleId
  }
}

export const toggleCollectionSelection = (collectionId) => {
  return {
    type: TOGGLE_COLLECTION_SELECTION,
    id: collectionId
  }
}

export const clearGranules = () => {
  return {
    type: CLEAR_GRANULES
  }
}

export const fetchingGranules = () => {
  return {
    type: FETCHING_GRANULES
  }
}

export const fetchedGranules = (granuleList) => {
  return {
    type: FETCHED_GRANULES,
    granules: granuleList
  }
}
