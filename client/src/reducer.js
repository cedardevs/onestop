import {combineReducers} from 'redux-seamless-immutable'

import cart from './reducers/cart'
import config from './reducers/config'
import errors from './reducers/errors'
import layout from './reducers/layout'
import routing from './reducers/routing'
import user from './reducers/user'

import collectionFilter from './reducers/search/collectionFilter'
import collectionRequest from './reducers/search/collectionRequest'
import collectionResult from './reducers/search/collectionResult'
import granuleFilter from './reducers/search/granuleFilter'
import granuleRequest from './reducers/search/granuleRequest'
import granuleResult from './reducers/search/granuleResult'
import info from './reducers/search/info'
import loading from './reducers/search/loading'

export const RESET_STORE = 'reset_store'

const search = combineReducers({
  collectionFilter,
  collectionRequest,
  collectionResult,
  granuleFilter,
  granuleRequest,
  granuleResult,
  info,
  loading,
})

// TODO: Pass search state elements to query removing the need for state duplication
const reducer = (state, action) => {
  // allow a top-level reducer action to trigger all reducers to initial state
  if (action.type === RESET_STORE) {
    state = undefined
  }
  return {
    cart: cart((state && state.cart) || undefined, action),
    config: config((state && state.config) || undefined, action),
    errors: errors((state && state.errors) || undefined, action),
    layout: layout((state && state.layout) || undefined, action),
    routing: routing((state && state.routing) || undefined, action),
    user: user((state && state.user) || undefined, action),
    search: search((state && state.search) || undefined, action),
  }
}

export default reducer
