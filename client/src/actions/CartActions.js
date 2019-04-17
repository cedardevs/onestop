// synchronous actions
export const CLEAR_SELECTED_GRANULES = 'CLEAR_SELECTED_GRANULES'
export const removeAllSelectedGranule = () => {
  return {
    type: CLEAR_SELECTED_GRANULES,
  }
}

export const INSERT_SELECTED_GRANULE = 'INSERT_SELECTED_GRANULE'
export const insertSelectedGranule = (item, itemId) => {
  return {
    type: INSERT_SELECTED_GRANULE,
    item: item,
    itemId: itemId,
  }
}

export const INSERT_MULTIPLE_SELECTED_GRANULES =
  'INSERT_MULTIPLE_SELECTED_GRANULES'
export const insertMultipleSelectedGranules = (items, itemIds) => {
  return {
    type: INSERT_MULTIPLE_SELECTED_GRANULES,
    items: items,
    itemIds: itemIds,
  }
}

export const REMOVE_SELECTED_GRANULE = 'REMOVE_SELECTED_GRANULE'
export const removeSelectedGranule = itemId => {
  return {
    type: REMOVE_SELECTED_GRANULE,
    itemId: itemId,
  }
}

export const REMOVE_MULTIPLE_SELECTED_GRANULES =
  'REMOVE_MULTIPLE_SELECTED_GRANULES'
export const removeMultipleSelectedGranules = itemIds => {
  return {
    type: REMOVE_MULTIPLE_SELECTED_GRANULES,
    itemIds: itemIds,
  }
}
