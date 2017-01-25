import { combineReducers } from 'redux-seamless-immutable'

import search from './behavior/search'
import routing from './behavior/routing'
import errors from './behavior/error'
import collectionRequest from './behavior/collectionRequest'
import granuleRequest from './behavior/granuleRequest'
import collectionSelect from './behavior/collectionSelect'

import config from './domain/config'
import results from './domain/results'

import cardDetails from './ui/cardDetails'
import loading from './ui/loading'
import granuleDetails from './ui/granuleDetails'

const domain = combineReducers({
  config,
  results
})

const ui = combineReducers({
  cardDetails,
  loading,
  granuleDetails
})

const behavior = combineReducers({
  collectionRequest,
  granuleRequest,
  collectionSelect,
  search
})

// TODO: Pass search state elements to query removing the need for state duplication
const reducer = (state, action) => {
  return {
    domain: domain(state && state.domain || undefined, action),
    behavior: behavior(state && state.behavior || undefined, action),
    ui: ui(state && state.ui || undefined, action),
    errors: errors(state && state.errors || undefined, action),
    routing: routing(state && state.routing || undefined, action, state && state.behavior && state.behavior.search)
  }
}

export default reducer
