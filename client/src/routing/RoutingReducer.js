import Immutable from 'immutable'
import { LOCATION_CHANGE } from 'react-router-redux'
import { INSTANTIATE_APP_STATE } from './RoutingActions'
import { SEARCH_COMPLETE } from '../search/SearchActions'
import { FETCHED_GRANULES } from '../result/granules/GranulesActions'
// Support query parsing

// Routing reducer
const initialState = Immutable.fromJS({
  locationBeforeTransitions: null,
  initialized: false
})

const routing = (state = initialState, action) => {
  switch (action.type) {
    case LOCATION_CHANGE:
      return state.merge({
        locationBeforeTransitions: action.payload
      })

    // Any actions which results in a view change
    case FETCHED_GRANULES:
    case SEARCH_COMPLETE:
      const { view, appState } = action
      let location = state.get('locationBeforeTransitions').toJS()
      location = Object.assign({}, location, {pathname: `/${view}`, action: 'PUSH',
        search: `?${encodeURIComponent(appState)}`})
      return state.merge({locationBeforeTransitions: location, 'initialized': true})

    default:
      return state
  }
}


export default routing
