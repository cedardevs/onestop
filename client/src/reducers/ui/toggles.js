import _ from 'lodash'
import Immutable from 'seamless-immutable'
import {TOGGLE_ABOUT, TOGGLE_HELP} from '../../actions/FlowActions'

export const initialState = Immutable({
  about: false,
  help: false
})

export const info = (state = initialState, action) => {
  switch(action.type) {
    case TOGGLE_ABOUT:
      return exclusiveToggle(state, 'about')

    case TOGGLE_HELP:
      return exclusiveToggle(state, 'help')

    default:
      return state
  }
}

// toggles the value of the given key, and if the new
// value is true, sets all other values to false
const exclusiveToggle = (state, toggleKey) => {
  const newStateForKey = !state[toggleKey]
  return _.reduce(state, (newState, value, key) => {
    if (key === toggleKey) {
      return Immutable.set(newState, key, newStateForKey)
    }
    const newValue = newStateForKey ? false : value
    return Immutable.set(newState, key, newValue)
  }, Immutable({}))
}

export default info
