import Immutable from 'seamless-immutable'
import { LOCATION_CHANGE } from 'react-router-redux'
import { SEARCH_COMPLETE } from '../../search/SearchActions'
import { FETCHED_GRANULES } from '../../result/granules/GranulesActions'

const initialState = Immutable({
  locationBeforeTransitions: null,
  initialized: false
})

const transition = (state = initialState, action, search = {}) => {
  switch (action.type) {
    case LOCATION_CHANGE:
      return Immutable.merge(state, {
        locationBeforeTransitions: action.payload
      })

    // Any actions which results in a view change
    case FETCHED_GRANULES:
    case SEARCH_COMPLETE:
      const { view } = action
      let location = state.locationBeforeTransitions
      location = Object.assign({}, location, {pathname: `/${view}`, action: 'PUSH',
        search: `?${JSON.stringify(search)}`})
      return Immutable.merge(state, {locationBeforeTransitions: location, initialized: true})

    default:
      return state
  }
}


export default transition
