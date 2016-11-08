import Immutable from 'immutable'
import { TOGGLE_GRANULE_FOCUS, TOGGLE_COLLECTION_SELECTION } from './GranulesActions'

export const initialState = Immutable.Map({
  selectedCollections: Immutable.Set(),
  focusedGranules: Immutable.Set(),
  granules: Immutable.Map()
})

export const granules = (state = initialState, action) => {
  switch(action.type) {
    case TOGGLE_COLLECTION_SELECTION:
      return state.set('selectedCollections', toggleValueInSet(state.get('selectedCollections'), action.id))

    case TOGGLE_GRANULE_FOCUS:
      return state.set('focusedGranules', toggleValueInSet(state.get('focusedGranules'), action.id))

    default:
      return state
  }
}

export default granules

const toggleValueInSet = (set, value) => {
  return set.has(value) ? set.delete(value) : set.add(value)
}