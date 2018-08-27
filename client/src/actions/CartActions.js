export const INSERT_SELECTED_GRANULE = 'INSERT_SELECTED_GRANULE'
export const insertSelectedGranule = (item, itemId) => {
  return {
    type: INSERT_SELECTED_GRANULE,
    item: item,
    itemId: itemId
  }
}

export const REMOVE_SELECTED_GRANULE = 'REMOVE_SELECTED_GRANULE'
export const removeSelectedGranule = (itemId) => {
  return {
    type: REMOVE_SELECTED_GRANULE,
    itemId: itemId,
  }
}