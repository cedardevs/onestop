import Immutable from 'seamless-immutable'
import {
  INSERT_MULTIPLE_SELECTED_GRANULES, INSERT_SELECTED_GRANULE, REMOVE_MULTIPLE_SELECTED_GRANULES,
  REMOVE_SELECTED_GRANULE,
} from '../../actions/CartActions'

export const initialState = Immutable({
  selectedGranules: {},
})

export const info = (state = initialState, action) => {
  switch (action.type) {
    case INSERT_SELECTED_GRANULE:
      const newInsertState = state.setIn(["selectedGranules", action.itemId], action.item)
      return newInsertState
    case INSERT_MULTIPLE_SELECTED_GRANULES:
      //TODO: implement state transition
      return state
    case REMOVE_SELECTED_GRANULE:
      const previousSelectedGranules = state.getIn(["selectedGranules"])
      const newSelectedGranules = previousSelectedGranules.without(action.itemId)
      const newRemoveState = state.set("selectedGranules", newSelectedGranules)
      return newRemoveState
    case REMOVE_MULTIPLE_SELECTED_GRANULES:
      //TODO: implement state transition
      return state
    default:
      return state
  }
}

export default info
