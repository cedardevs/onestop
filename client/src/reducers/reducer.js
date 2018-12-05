import {combineReducers} from 'redux-seamless-immutable'

import search from './behavior/search'
import routing from './behavior/routing'
import errors from './behavior/error'
import request from './behavior/request'

import granules from './cart/granules'

import config from './domain/config'
import info from './domain/info'
import results from './domain/results'

import loading from './ui/loading'
import layout from './ui/layout'

import user from './user/user'

export const RESET_STORE = 'reset_store'

const domain = combineReducers({
  config,
  info,
  results,
  user,
})

const ui = combineReducers({
  loading,
  layout,
})

const behavior = combineReducers({
  request,
  search,
  routing,
  errors,
})

const cart = combineReducers({
  granules,
})

// TODO: Pass search state elements to query removing the need for state duplication
const reducer = (state, action) => {
  // allow a top-level reducer action to trigger all reducers to initial state
  if (action.type === RESET_STORE) {
    state = undefined
  }
  return {
    domain: domain((state && state.domain) || undefined, action),
    behavior: behavior((state && state.behavior) || undefined, action),
    ui: ui((state && state.ui) || undefined, action),
    cart: cart((state && state.cart) || undefined, action),
  }
}

export default reducer
