import Immutable from 'seamless-immutable'
import {LOCATION_CHANGE} from 'connected-react-router'

const initialState = Immutable({
  locationBeforeTransitions: null,
})

const routing = (state = initialState, action) => {
  switch (action.type) {
    case LOCATION_CHANGE:
      return Immutable.merge(state, {locationBeforeTransitions: action.payload})
    default:
      return state
  }
}

export default routing
