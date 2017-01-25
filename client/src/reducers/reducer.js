import { combineReducers } from 'redux-seamless-immutable'

import search from './appState/search'
import routing from './appState/routing'
import errors from './appState/error'
import collectionRequest from './appState/collectionRequest'
import granuleRequest from './appState/granuleRequest'
import collectionSelect from './appState/collectionSelect'

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

const appState = combineReducers({
  collectionRequest,
  granuleRequest,
  collectionSelect,
  search
})

// TODO: Pass search state elements to query removing the need for state duplication
const reducer = (state, action) => {
  return {
    domain: domain(state && state.domain || undefined, action),
    appState: appState(state && state.appState || undefined, action),
    ui: ui(state && state.ui || undefined, action),
    errors: errors(state && state.errors || undefined, action),
    routing: routing(state && state.routing || undefined, action, state && state.appState && state.appState.search)
  }
}

export default reducer
