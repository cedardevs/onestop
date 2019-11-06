import Immutable from 'seamless-immutable'
import {initialState as initialStateCart} from './reducers/cart'
import {storageAvailable} from './utils/localStorageUtil'

const isObject = obj => {
  return typeof obj === 'object' && obj !== null
}

const isEmptyObject = obj => {
  return Object.entries(obj).length === 0 && obj.constructor === Object
}

const getKey = (obj, key) => {
  // return null if key cannot be found for whatever reason
  if (obj === null || obj === undefined || obj[key] === undefined) {
    return null
  }
  return isObject(obj) && !isEmptyObject(obj) && key in obj
    ? obj[key]
    : obj[key]
}

// immutable (returns a new object with new value at key -- or index, if array)
const setKey = (obj, key, value) => {
  // return new array with value at index (key) if obj is an array or key is an integer
  if (Array.isArray(obj) || Number.isInteger(key)) {
    return Object.assign([], obj, {[Number.parseInt(key)]: value})
  }
  // return sole key/value object if object DNE; otherwise, merge key/value
  return !obj ? {[key]: value} : {...obj, [key]: value}
}

export const getValue = (obj, path) => {
  if (!path || path.length === 0) {
    // return original object w/o path
    return obj
  }
  else if (path.length === 1) {
    // return the leaf at the end of the path
    return getKey(obj, path[0])
  }
  // branch into path
  const branch = getKey(obj, path.shift())
  return getValue(branch, path)
}

// immutable (returns a new version where the new value at path is set)
export const setValue = (obj, path, value) => {
  if (!path || path.length === 0) {
    // replace entire object w/o path
    return value
  }
  else if (path.length === 1) {
    if (value === undefined) {
      // undefined values means we want to prune this path (immutably)
      let {[path[0]]: omit, ...res} = obj
      return res
    }
    else {
      // set the leaf value at the end of the path
      return setKey(obj, path[0], value)
    }
  }

  // branch into path
  const branchKey = path.shift()
  const branch = getKey(obj, branchKey)
  return setKey(obj, branchKey, setValue(branch, path, value))
}

// load state from browser local storage
export function loadReducerState(reducerKey, initialState = Immutable()){
  if (storageAvailable('localStorage')) {
    try {
      let serializedState = localStorage.getItem(reducerKey)
      if (serializedState === null) {
        return initialState
      }
      return JSON.parse(serializedState)
    } catch (err) {
      return initialState
    }
  }
  return initialState
}

// we can save a specific reducer property in local storage, without needing to cache every value in that reducer
// as long as we use a consistent initial state in the reducer and local storage (serialized)
// this ensures, that when we refresh and pre-load our state that *is* in local storage, it does not differ from
// our initial conditions in redux, generally
export function saveReducerStateValue(
  reducerKey,
  path,
  value,
  initialState = Immutable()
){
  try {
    // deserialize the top-level local storage key associated with the reducer we want to cache
    // fallback to an initial state (should be borrowed from reducer definition)` if nothing could be loaded
    const currentState = loadReducerState(reducerKey, initialState)

    // we can immutably set a value on our current state to update the specific value to cache/pre-load on refresh
    const saveState = setValue(currentState, path, value)

    // local storage stores information in serialized JSON format
    let serializedState = JSON.stringify(saveState)

    // make sure we set our new saved state on the right reducer key so that it can be pre-loaded as-needed
    localStorage.setItem(reducerKey, serializedState)
  } catch (err) {}
}

// this function pre-loads our application state from local storage for all the reducers we care to have cached
// in other words, this restores the state for items which we explicitly called `saveReducerStateValue` on when
// it is not desired for a browser refresh to obliterate that information in redux
export function preloadState(){
  return Immutable({
    // restore the cart reducer (specifically the granules that have been added to the cart)
    cart: loadReducerState('cart', initialStateCart),
  })
}
