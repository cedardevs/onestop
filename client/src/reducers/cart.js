import Immutable from 'seamless-immutable'
import {
  INSERT_MULTIPLE_SELECTED_GRANULES,
  INSERT_SELECTED_GRANULE,
  REMOVE_MULTIPLE_SELECTED_GRANULES,
  REMOVE_SELECTED_GRANULE,
  CLEAR_SELECTED_GRANULES,
} from '../actions/CartActions'
import {
  GRANULES_FOR_CART_CLEAR_ERROR,
  GRANULES_FOR_CART_ERROR,
  GRANULES_FOR_CART_RESULTS_RECEIVED,
} from '../actions/routing/GranuleSearchStateActions'
import {mergeGranulesArrayIntoGranulesMap} from '../utils/resultUtils'

export const initialState = Immutable({
  selectedGranules: {},
  error: null,
})

const newGranulesForCartResultsReceived = (state, action) => {
  let newGranules = mergeGranulesArrayIntoGranulesMap(
    action.granules,
    state.selectedGranules
  )
  return Immutable.merge(state, {
    selectedGranules: newGranules,
    error: null,
  })
}

export const cart = (state = initialState, action) => {
  switch (action.type) {
    case GRANULES_FOR_CART_RESULTS_RECEIVED:
      return newGranulesForCartResultsReceived(state, action)

    case GRANULES_FOR_CART_ERROR:
      return state.setIn([ 'error' ], action.warning)

    case GRANULES_FOR_CART_CLEAR_ERROR:
      return state.setIn([ 'error' ], initialState.error)

    case INSERT_SELECTED_GRANULE:
      const newInsertState = state.setIn(
        [ 'selectedGranules', action.itemId ],
        action.item
      )
      return newInsertState
    case INSERT_MULTIPLE_SELECTED_GRANULES:
      //TODO: implement state transition
      return state
    case REMOVE_SELECTED_GRANULE:
      const previousSelectedGranules = state.getIn([ 'selectedGranules' ])
      const newSelectedGranules = previousSelectedGranules.without(
        action.itemId
      )
      const newRemoveState = state.set('selectedGranules', newSelectedGranules)
      return newRemoveState
    case REMOVE_MULTIPLE_SELECTED_GRANULES:
      //TODO: implement state transition
      return state
    case CLEAR_SELECTED_GRANULES:
      const newRemoveAllState = state.set(
        'selectedGranules',
        initialState.selectedGranules
      )
      // delete state['selectedGranules']
      return newRemoveAllState
    default:
      return state
  }
}

export default cart
