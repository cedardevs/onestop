// local storage utilities
export const storageAvailable = type => {
  try {
    var storage = window[type],
      x = '__storage_test__'
    storage.setItem(x, x)
    storage.removeItem(x)
    return true
  } catch (e) {
    return (
      e instanceof DOMException &&
      // everything except Firefox
      (e.code === 22 ||
        // Firefox
        e.code === 1014 ||
        // test name field too, because code might not be present
        // everything except Firefox
        e.name === 'QuotaExceededError' ||
        // Firefox
        e.name === 'NS_ERROR_DOM_QUOTA_REACHED') &&
      // acknowledge QuotaExceededError only if there's something already stored
      storage.length !== 0
    )
  }
}

const getObjectFromLocalStorage = (keyToObject, fallback) => {
  if (storageAvailable('localStorage')) {
    try {
      const parsedObject = JSON.parse(localStorage.getItem(keyToObject))
      return parsedObject ? parsedObject : {}
    } catch (e) {}
    return {}
  }
  return fallback
}

const getListFromLocalStorage = (keyToList, fallback) => {
  if (storageAvailable('localStorage')) {
    try {
      const parsedList = JSON.parse(localStorage.getItem(keyToList))
      return parsedList ? parsedList : []
    } catch (e) {}
    return []
  }
  return fallback
}

// ---- OLD STUFF ----
// selected granules (to be added to cart) in local storage
const KEY_SELECTED_GRANULES = 'selectedGranules'
export const getSelectedGranulesFromStorage = state => {
  // fallback to the redux state of selected cart granules, if local storage unavailable
  return getObjectFromLocalStorage(
    KEY_SELECTED_GRANULES,
    state.cart.selectedGranules
  )
}

export const insertSelectedGranuleIntoLocalStorage = (itemId, item) => {
  const selectedGranules = getObjectFromLocalStorage(KEY_SELECTED_GRANULES, {})
  const newSelectedGranules = {...selectedGranules, [itemId]: item}
  localStorage.setItem(
    KEY_SELECTED_GRANULES,
    JSON.stringify(newSelectedGranules)
  )
}

export const insertSelectedGranulesIntoLocalStorage = granules => {
  const selectedGranules = getObjectFromLocalStorage(KEY_SELECTED_GRANULES, {})

  // loop through the granule "data" array returned from a search API granule success payload
  let insertedGranules = {}
  granules.forEach(g => {
    insertedGranules[g.id] = g.attributes
  })

  // set each granule into local storage,
  // overriding any previous conflicting granule ids with new
  const newSelectedGranules = {...selectedGranules, ...insertedGranules}
  localStorage.setItem(
    KEY_SELECTED_GRANULES,
    JSON.stringify(newSelectedGranules)
  )
}

export const removeSelectedGranuleFromLocalStorage = itemId => {
  const selectedGranules = getObjectFromLocalStorage(KEY_SELECTED_GRANULES, {})
  delete selectedGranules[itemId]
  localStorage.setItem(KEY_SELECTED_GRANULES, JSON.stringify(selectedGranules))
}

export const removeAllSelectedGranulesFromLocalStorage = () => {
  localStorage.setItem('selectedGranules', JSON.stringify({}))
}
