import {combineReducers} from 'redux-seamless-immutable'

import search from './behavior/search'
import routing from './behavior/routing'
import errors from './behavior/error'
import request from './behavior/request'

import {api} from './domain/api'
import config from './domain/config'
import info from './domain/info'
import results from './domain/results'

import loading from './ui/loading'
import background from './ui/background'
import layout from './ui/layout'

import granules from './cart/granules'

const domain = combineReducers({
  api,
  config,
  info,
  results,
})

const ui = combineReducers({
  loading,
  background,
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
  return {
    domain: domain((state && state.domain) || undefined, action),
    behavior: behavior((state && state.behavior) || undefined, action),
    ui: ui((state && state.ui) || undefined, action),
    cart: cart((state && state.cart) || undefined, action),
  }
}

export default reducer
