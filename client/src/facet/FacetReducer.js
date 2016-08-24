import Immutable from 'immutable';
import {OPEN, CLOSED} from './FacetActions'
import { METADATA_RECEIVED } from './FacetActions'

export const initialState = Immutable.fromJS({
  categories: null
})

const facets = (state = initialState, action) => {
  switch(action.type) {
    case METADATA_RECEIVED:
      return state.mergeDeep({
        categories: action.metadata.facets
      })
    default:
      return state
  }
}

export default facets
