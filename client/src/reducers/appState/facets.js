import Immutable from 'seamless-immutable';
import { TOGGLE_FACET } from '../../search/facet/FacetActions'

export const initialState = Immutable({
  selectedFacets: {}
})

export const facets = (state = initialState, action) => {
  switch(action.type) {
    case TOGGLE_FACET:
      return Immutable.set(state, 'selectedFacets', action.selectedFacets)

    default:
      return state
  }
}

export default facets
