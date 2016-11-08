export const TOGGLE_GRANULE_FOCUS = 'toggle_granule_focus'
export const TOGGLE_COLLECTION_SELECTION = 'toggle_collection_selection'
export const RETRIEVING_GRANULES = 'retrieving_granules'
export const RECEIVING_GRANULES = 'receiving_granules'
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

export const retrievingGranules = () => {
  return {
    type: RETRIEVING_GRANULES
  }
}

export const receivingGranules = (granuleList) => {
  return {
    type: RECEIVING_GRANULES,
    granules: granuleList
  }
}

export const clearGranules = () => {
  return {
    type: CLEAR_GRANULES
  }
}
