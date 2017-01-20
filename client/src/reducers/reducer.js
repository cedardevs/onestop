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
  if (action.type === 'CLEAR_FACETS') { state.facets = undefined }
  return {
    search: search(state.search, action),
    facets: facets(state.facets, action)
  }
}

// TODO: Pass search state elements to query removing the need for state duplication
const reducer = (state, action) => {
  return {
    domain: domain(state && state.domain || undefined, action),
    appState: appState(state && state.appState || undefined, action),
    searchAndFacets: searchAndFacets(state && state.searchAndFacets || undefined, action),
    ui: ui(state && state.ui || undefined, action),
    query: query(state && state.query || undefined, action),
    errors: errors(state && state.errors || undefined, action),
    routing: routing(state && state.routing || undefined, action)
  }
}

export default reducer
