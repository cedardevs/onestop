import {combineReducers} from 'redux-seamless-immutable'

import search from './reducers/behavior/search'
import routing from './reducers/behavior/routing'
import errors from './reducers/behavior/error'
import request from './reducers/behavior/request'

import config from './reducers/domain/config'
import info from './reducers/domain/info'
import results from './reducers/domain/results'

import loading from './reducers/ui/loading'

///

import layout from './reducers/layout'
import cart from './reducers/cart'
import user from './reducers/user'

export const RESET_STORE = 'reset_store'

const domain = combineReducers({
  config,
  info,
  results,
})

const ui = combineReducers({
  loading,
})

const behavior = combineReducers({
  request,
  search,
  routing,
  errors,
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
    //
    layout: layout((state && state.layout) || undefined, action),
    cart: cart((state && state.cart) || undefined, action),
    user: user((state && state.user) || undefined, action),
  }
}

export default reducer
