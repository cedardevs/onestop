export const TOGGLE_GRANULE_FOCUS = 'toggle_granule_focus'
export const TOGGLE_COLLECTION_SELECTION = 'toggle_collection_selection'

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