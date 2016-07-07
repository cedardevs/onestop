import Immutable from 'immutable'
import search from './../search/SearchReducer'
import results from './../result/ResultReducer'
import details from './../detail/DetailReducer'
import facets from './../facet/FacetReducer'
import { routerReducer } from 'react-router-redux'

const reducer = (state = Immutable.Map(), action) => {
  return state.merge({
    search: search(state.get('search'), action),
    facets: facets(state.get('facets'), action),
    results: results(state.get('results'), action),
    details: details(state.get('details'), action),
    routing: routerReducer
  })
}

export default reducer
