import Immutable from 'immutable'
import { LOCATION_CHANGE } from 'react-router-redux'
import { SET_OPERATION } from './RoutingActions'

// Routing reducer
const initialState = Immutable.fromJS({
  locationBeforeTransitions: null
})

const routing = (state = initialState, action) => {
  switch (action.type) {
    case LOCATION_CHANGE:
    console.log("this when", action.payload)
    return state.merge({
      locationBeforeTransitions: action.payload
    })
    case SET_OPERATION:
      console.log(state.toJS())
      // const { name } = action
      let location = state.locationBeforeTransitions
      // const pathname = `/${name}`
      // location = Object.assign({}, location, {pathname}, {action: 'PUSH'})
      return state.set('locationBeforeTransitions', location)
    default:
    return state
  }
}

export default routing
