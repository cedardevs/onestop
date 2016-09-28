import Immutable from 'immutable'
import { combineReducers } from 'redux-immutable'
import search from './search/SearchReducer'
import results from './result/ResultReducer'
import details from './detail/DetailReducer'
import facets from './search/facet/FacetReducer'
import temporal from './search/temporal/TemporalReducer'
import { LOCATION_CHANGE } from 'react-router-redux'

// Routing reducer
const initialState = Immutable.fromJS({
  locationBeforeTransitions: null
})

const routing = (state = initialState, action) => {
  switch (action.type) {
    case LOCATION_CHANGE:
    return state.merge({
      locationBeforeTransitions: action.payload
    })
    default:
    return state
  }
}

const reducer = combineReducers({
  search,
  results,
  details,
  routing,
  facets,
  temporal
})

export default reducer
