import { combineReducers } from 'redux-seamless-immutable'

import facets from './appState/facets'
import geometry from './appState/geometry'
import routing from './appState/routing'
import queryText from './appState/queryText'
import temporal from './appState/temporal'
import errors from './appState/error'
import collectionRequest from './appState/collectionRequest'
import granuleRequest from './appState/granuleRequest'
import collectionSelect from './appState/collectionSelect'

import config from './domain/config'
import query from './domain/query'
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
  collectionSelect
})

const search = combineReducers({
  geometry,
  queryText,
  temporal
})

const searchAndFacets = (state = {}, action) => {
  if (action.type === 'CLEAR_SEARCH') { state.search = undefined }
  return {
    search: search(state.search, action),
    facets: facets(state.facets, action)
  }
}

// TODO: Pass search state elements to query removing the need for state duplication
const reducer = (state, action) => {
  if (action.type === 'CLEAR_FACETS') { state.searchAndFacets = undefined }
  return {
    domain: domain(state.domain, action),
    appState: appState(state.appState, action),
    searchAndFacets: searchAndFacets(state.searchAndFacets, action),
    ui: ui(state.ui, action),
    query: query(state.query, action),
    errors: errors(state.errors, action),
    routing: routing(state.routing, action)
  }
}

export default reducer
