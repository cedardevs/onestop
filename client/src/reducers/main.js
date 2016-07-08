import Immutable from 'immutable'
import search from './../search/SearchReducer'
import results from './../result/ResultReducer'
import details from './../detail/DetailReducer'
import facets from './../facet/FacetReducer'
import { LOCATION_CHANGE } from 'react-router-redux'

// Routing reducer
const initialState = Immutable.Map({})
const routing = (state = initialState, action) => {
  switch (action.type) {
    case LOCATION_CHANGE:
      state.set("routing", {locationBeforeTransitions: action.payload})
      return state
    default:
      return state
  }
}

const reducer = (state = Immutable.Map(), action) => {
  return state.merge({
    search: search(state.get('search'), action),
    facets: facets(state.get('facets'), action),
    results: results(state.get('results'), action),
    details: details(state.get('details'), action),
    routing: routing(state.get('routing'), action)
  })
}

export default reducer
