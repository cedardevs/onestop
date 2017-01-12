import Immutable from 'seamless-immutable'
import _ from 'lodash'
import { UPDATE_QUERY } from '../../search/SearchActions'

export const initialState = Immutable({
  text: ''
})

export const queryText = (state = initialState, action) => {
  let newState

  switch (action.type) {
    case UPDATE_QUERY:
      return Immutable.merge(state, {text: action.searchText})

    default:
      return state
  }
}

export default queryText
