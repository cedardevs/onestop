import Immutable from 'seamless-immutable'
import { TOGGLE_GRANULE_FOCUS } from '../../actions/FlowActions'

export const initialState = Immutable({
  focusedGranules: []
})

export const granuleDetails = (state = initialState, action) => {
  switch (action.type) {
    case TOGGLE_GRANULE_FOCUS:
      return Immutable.set(state, 'focusedGranules', action.focused ? [action.id] : [])

    default:
      return state
  }
}

export default granuleDetails
