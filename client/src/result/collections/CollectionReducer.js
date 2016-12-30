import Immutable from 'seamless-immutable'
import _ from 'lodash'
import {SEARCH, SEARCH_COMPLETE} from '../../search/SearchActions'
import { TOGGLE_SELECTION, CLEAR_SELECTIONS } from './CollectionsActions'

export const initialState = Immutable({
  results: {},
  selectedIds: []
})

export const collections = (state = initialState, action) => {
  switch(action.type) {
    case TOGGLE_SELECTION:
      return Immutable.set(state, 'selectedIds', toggleId(state.selectedIds, action.id))

    case CLEAR_SELECTIONS:
      return Immutable.set(state, 'selectedIds', initialState.selectedIds)

    case SEARCH:
      return Immutable.set(state, 'results', initialState.results)

    case SEARCH_COMPLETE:
      let results = Immutable({})
      action.items.forEach((val, key) => {
        results = Immutable.set(results, key, val)
      })
      return Immutable.set(state, 'results', results)

    default:
      return state
  }
}

export default collections

const toggleId = (selectedIds, value, idx = 0) => {
  if (idx === selectedIds.length){
    return selectedIds.concat([value])
  } else if (selectedIds[idx] === value){
    return selectedIds.slice(0, idx).concat(selectedIds.slice(idx + 1))
  } else { return toggleId(selectedIds, value, idx + 1) }
}
