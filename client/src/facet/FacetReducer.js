import Immutable from 'immutable';
import {OPEN, CLOSED} from './FacetActions'
import { METADATA_RECEIVED } from './FacetActions'
export const initialState = Immutable.Map({
  visible: false
})

const facets = (state = initialState, action) => {
  switch(action.type) {
    case METADATA_RECEIVED:
      return state
    default:
      return state
  }
}

export default facets
