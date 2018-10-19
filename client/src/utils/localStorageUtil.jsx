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

export const insertGranule = (itemId, item) => {
  let selectedGranules = localStorage.getItem('selectedGranules')
    ? JSON.parse(localStorage.getItem('selectedGranules'))
    : {}
  let cartGranule = {}
  cartGranule[itemId] = item
  localStorage.setItem(
    'selectedGranules',
    JSON.stringify({...selectedGranules, ...cartGranule})
  )
}

export const removeGranuleFromLocalStorage = itemId => {
  let selectedGranules = JSON.parse(localStorage.getItem('selectedGranules'))
  delete selectedGranules[itemId]
  localStorage.setItem(
    'selectedGranules',
    JSON.stringify({...selectedGranules})
  )
}

export const getSelectedGranulesFromStorage = state => {
  if (storageAvailable('localStorage')) {
    try {
      return JSON.parse(localStorage.getItem('selectedGranules'))
    } catch (e) {}
    return {}
  }
  else {
    state.cart.granules.selectedGranules
  }
}

export const removeAllGranulesFromLocalStorage = () => {
  localStorage.setItem('selectedGranules', JSON.stringify({}))
}
