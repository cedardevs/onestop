import Immutable from 'immutable'
import { combineReducers } from 'redux-immutable'
import config from './config/ConfigReducer'
import search from './search/SearchReducer'
import collections from './result/collections/CollectionReducer'
import details from './detail/DetailReducer'
import facets from './search/facet/FacetReducer'
import map from './search/map/MapReducer'
import temporal from './search/temporal/TemporalReducer'
import loading from './loading/LoadingReducer'
import errors from './error/ErrorReducer'
import granules from './result/granules/GranulesReducer'
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
  config,
  search,
  collections,
  details,
  routing,
  facets,
  temporal,
  map,
  loading,
  errors,
  granules
})

export default reducer
