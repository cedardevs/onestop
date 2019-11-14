import {connectRouter} from 'connected-react-router'
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

import collectionDetailFilter from './reducers/search/collectionDetailFilter'
import collectionDetailRequest from './reducers/search/collectionDetailRequest'
import collectionDetailResult from './reducers/search/collectionDetailResult'

import granuleFilter from './reducers/search/granuleFilter'
import granuleRequest from './reducers/search/granuleRequest'
import granuleResult from './reducers/search/granuleResult'
import info from './reducers/search/info'

export const RESET_STORE = 'reset_store'

const search = combineReducers({
  collectionFilter,
  collectionRequest,
  collectionResult,
  collectionDetailFilter,
  collectionDetailRequest,
  collectionDetailResult,
  granuleFilter,
  granuleRequest,
  granuleResult,
  info,
})

const appReducer = history =>
  combineReducers({
    router: connectRouter(history),
    cart: cart,
    config: config,
    errors: errors,
    layout: layout,
    routing: routing,
    user: user,
    search: search,
  })

export default history => (state, action) => {
  // allow a top-level reducer action to trigger all reducers to initial state
  if (action.type === RESET_STORE) {
    state = undefined
  }
  return appReducer(history)(state, action)
}
