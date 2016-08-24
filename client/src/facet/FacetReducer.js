import Immutable from 'immutable';
import {OPEN, CLOSED} from './FacetActions'
import { METADATA_RECEIVED } from './FacetActions'

export const initialState = Immutable.fromJS({
  facets: null
})

const facets = (state = initialState, action) => {
  switch(action.type) {
    case METADATA_RECEIVED:
      let tempState = state.mergeDeep({
        facets: action.metadata.facets
      })
      console.log(tempState.toJS())
      return tempState
    default:
      return state
  }
}

export default facets
