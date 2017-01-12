import Immutable from 'seamless-immutable'
import {
    TOGGLE_GRANULE_FOCUS, FETCHING_GRANULES, FETCHED_GRANULES, CLEAR_GRANULES
} from './GranulesActions'

export const initialState = Immutable({
  selectedCollections: [],
  focusedGranules: [],
  granules: {},
  inFlight: false
})

export const granules = (state = initialState, action) => {
  switch(action.type) {
    case TOGGLE_GRANULE_FOCUS:
      return Immutable.set(state, 'focusedGranules', toggleId(state.focusedGranules, action.id))

    case FETCHING_GRANULES:
      return Immutable.set(state, 'inFlight', true)

    case FETCHED_GRANULES:
      const newGranules = action.granules.reduce(
          (existing, next) => existing.set(next.id, next.attributes),
          state.granules)
      return Immutable.set((Immutable.set(state, 'inFlight', false)), 'granules', newGranules)

    case CLEAR_GRANULES:
      return Immutable.set(state, 'granules', initialState.granules)

    default:
      return state
  }
}

export default granules

const toggleId = (selectedIds, value, idx = 0) => {
  if (idx === selectedIds.length){
    return selectedIds.concat([value])
  } else if (selectedIds[idx] === value){
    return selectedIds.slice(0, idx).concat(selectedIds.slice(idx + 1))
  } else { return toggleId(selectedIds, value, idx + 1) }
}
