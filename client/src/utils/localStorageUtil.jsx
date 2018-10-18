//
// const storageAvailable = type => {
//     try {
//         var storage = window[type],
//             x = '__storage_test__'
//         storage.setItem(x, x);
//         storage.removeItem(x);
//         return true;
//     }
//     catch(e) {
//         return e instanceof DOMException && (
//                 // everything except Firefox
//             e.code === 22 ||
//             // Firefox
//             e.code === 1014 ||
//             // test name field too, because code might not be present
//             // everything except Firefox
//             e.name === 'QuotaExceededError' ||
//             // Firefox
//             e.name === 'NS_ERROR_DOM_QUOTA_REACHED') &&
//             // acknowledge QuotaExceededError only if there's something already stored
//             storage.length !== 0;
//     }
// }
//
// const insertGranule = (itemId, item) => {
//     var selectedGranules = JSON.parse(localStorage.getItem('selectedGranule'))
//     localStorage.setItem('selectedGranules', JSON.stringify(Object.assign({itemId: item}, selectedGranules)))
// }
//
// const removeGranule = (itemId) => {
//     var selectedGranules = JSON.parse(localStorage.getItem('selectedGranule'))
//     selectedGranules.removeItem(itemId)
//     localStorage.setItem('selectedGranules', JSON.stringify(Object.assign({}, selectedGranules)))
// }
//
// const getSelectedGranulesFromStorage = {
//     return JSON.parse(localStorage.getItem('selectedGranule'))
// }
//
// const getSelectedGranulesFromStorage = {
//     return JSON.parse(localStorage.getItem('selectedGranule'))
// }

export const storageAvailable = type => {
    try {
        var storage = window[type],
            x = '__storage_test__'
        storage.setItem(x, x)
        storage.removeItem(x)
        return true
    }
    catch(e) {
        return e instanceof DOMException && (
                // everything except Firefox
            e.code === 22 ||
            // Firefox
            e.code === 1014 ||
            // test name field too, because code might not be present
            // everything except Firefox
            e.name === 'QuotaExceededError' ||
            // Firefox
            e.name === 'NS_ERROR_DOM_QUOTA_REACHED') &&
            // acknowledge QuotaExceededError only if there's something already stored
            storage.length !== 0
    }
}

export const insertGranule = (itemId, item) => {
    let selectedGranules =  localStorage.getItem('selectedGranules') ? JSON.parse(localStorage.getItem('selectedGranules')) : {}
    console.log("Inserting: " + itemId)
    console.log(selectedGranules)
    let cartGranule = {}
    cartGranule[itemId] = item
    console.log("selectedGranules:", JSON.stringify(selectedGranules, null ,4))
    console.log("cartGranules:", JSON.stringify(cartGranule, null, 4))
    // localStorage.setItem('selectedGranules', JSON.stringify(Object.assign(cartGranule, selectedGranules)))
    localStorage.setItem('selectedGranules', JSON.stringify({...selectedGranules, ...cartGranule}))
//
}

export const removeGranule = (itemId) => {
    let selectedGranules = JSON.parse(localStorage.getItem('selectedGranules'))
    delete selectedGranules[itemId]
    // localStorage.setItem('selectedGranules', JSON.stringify(Object.assign(selectedGranules, {})))
    localStorage.setItem('selectedGranules', JSON.stringify({...selectedGranules, ...cartGranule}))

}

export const getSelectedGranulesFromStorage = () => {
    console.log(JSON.parse(localStorage.getItem('selectedGranules')))
    return JSON.parse(localStorage.getItem('selectedGranules'))
}

export const clearGranulesFromStorage  = () => {
    localStorage.setItem('selectedGranules', {})
}