import Immutable from 'immutable'
import {SEARCH, SEARCH_COMPLETE, COUNT_HITS} from '../../search/SearchActions'
import { TOGGLE_SELECTION, CLEAR_SELECTIONS } from './CollectionsActions'

export const initialState = Immutable.Map({
  results: Immutable.Map(),
  selectedIds: Immutable.Set(),
  totalHits: 0
})

export const collections = (state = initialState, action) => {
  switch(action.type) {
    case TOGGLE_SELECTION:
      return state.set('selectedIds', toggleValueInSet(state.get('selectedIds'), action.id))

    case CLEAR_SELECTIONS:
      return state.set('selectedIds', initialState.get('selectedIds'))

    case SEARCH:
      return state.set('results', initialState.get('results'))

    case SEARCH_COMPLETE:
      let results = Immutable.Map()
      action.items.forEach((value, key) => {
        results = results.set(key, Immutable.Map(value))
      })
      return state.set('results', results)

    case COUNT_HITS:
      return state.set('totalHits', action.totalHits)

    default:
      return state
  }
}

export default collections

const toggleValueInSet = (set, value) => {
  return set.has(value) ? set.delete(value) : set.add(value)
}
