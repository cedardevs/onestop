import Immutable from 'seamless-immutable'
import { TOGGLE_SELECTION, CLEAR_SELECTIONS } from '../../result/collections/CollectionsActions'

export const initialState = Immutable({
  selectedIds: []
})

export const collectionSelect = (state = initialState, action) => {
  switch(action.type) {
    case TOGGLE_SELECTION:
      return Immutable.set(state, 'selectedIds', toggleId(state.selectedIds, action.id))

    case CLEAR_SELECTIONS:
      return Immutable.set(state, 'selectedIds', initialState.selectedIds)

    default:
      return state
  }
}

export default collectionSelect

const toggleId = (selectedIds, value, idx = 0) => {
  if (idx === selectedIds.length){
    return selectedIds.concat([value])
  } else if (selectedIds[idx] === value){
    return selectedIds.slice(0, idx).concat(selectedIds.slice(idx + 1))
  } else { return toggleId(selectedIds, value, idx + 1) }
}
