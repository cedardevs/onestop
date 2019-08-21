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

export const insertGranules = granules => {
  // loop through the granule "data" array returned from a search API granule success payload
  granules.forEach(g => {
    let selectedGranules = localStorage.getItem('selectedGranules')
      ? JSON.parse(localStorage.getItem('selectedGranules'))
      : {}

    const itemId = g.id
    const item = g.attributes
    let cartGranule = {}
    cartGranule[itemId] = item

    // set each granule into local storage,
    // overriding any previous conflicting granule ids with new
    localStorage.setItem(
      'selectedGranules',
      JSON.stringify({...selectedGranules, ...cartGranule})
    )
  })
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
      const parsedSelectedGranules = JSON.parse(
        localStorage.getItem('selectedGranules')
      )
      return parsedSelectedGranules ? parsedSelectedGranules : {}
    } catch (e) {}
    return {}
  }
  else {
    return state.cart.granules.selectedGranules
  }
}

export const removeAllGranulesFromLocalStorage = () => {
  localStorage.setItem('selectedGranules', JSON.stringify({}))
}
