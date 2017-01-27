import Immutable from 'seamless-immutable'
import { TOGGLE_GRANULE_FOCUS } from '../../search/SearchActions'

export const initialState = Immutable({
  focusedGranules: []
})

export const granuleDetails = (state = initialState, action) => {
  switch(action.type) {
    case TOGGLE_GRANULE_FOCUS:
      return Immutable.set(state, 'focusedGranules', toggleId(state.focusedGranules, action.id))

    default:
      return state
  }
}

const toggleId = (selectedIds, value, idx = 0) => {
  if (idx === selectedIds.length){
    return selectedIds.concat([value])
  } else if (selectedIds[idx] === value){
    return selectedIds.slice(0, idx).concat(selectedIds.slice(idx + 1))
  } else { return toggleId(selectedIds, value, idx + 1) }
}

export default granuleDetails
