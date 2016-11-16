export const TOGGLE_SELECTION = 'toggle_selection'
export const CLEAR_SELECTIONS = 'clear_selections'

export const toggleSelection = (collectionId) => {
  return {
    type: TOGGLE_SELECTION,
    id: collectionId
  }
}

export const clearSelections = () => {
  return {
    type: CLEAR_SELECTIONS
  }
}