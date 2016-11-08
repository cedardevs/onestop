import Immutable from 'immutable'
import {
    TOGGLE_GRANULE_FOCUS, TOGGLE_COLLECTION_SELECTION,
    FETCHING_GRANULES, FETCHED_GRANULES, CLEAR_GRANULES
} from './GranulesActions'

export const initialState = Immutable.Map({
  selectedCollections: Immutable.Set(),
  focusedGranules: Immutable.Set(),
  granules: Immutable.Map(),
  inFlight: false
})

export const granules = (state = initialState, action) => {
  switch(action.type) {
    case TOGGLE_COLLECTION_SELECTION:
      return state.set('selectedCollections', toggleValueInSet(state.get('selectedCollections'), action.id))

    case TOGGLE_GRANULE_FOCUS:
      return state.set('focusedGranules', toggleValueInSet(state.get('focusedGranules'), action.id))

    case FETCHING_GRANULES:
      return state.set('inFlight', true)

    case FETCHED_GRANULES:
      const newGranules = Immutable.fromJS(action.granules).reduce(
          (existing, next) => existing.set(next.get('id'), next),
          state.get('granules'))
      return state.set('inFlight', false).set('granules', newGranules)

    case CLEAR_GRANULES:
      return state.set('granules', initialState.get('granules'))

    default:
      return state
  }
}

export default granules

const toggleValueInSet = (set, value) => {
  return set.has(value) ? set.delete(value) : set.add(value)
}
