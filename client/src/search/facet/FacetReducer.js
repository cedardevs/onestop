import Immutable from 'seamless-immutable';
import _ from 'lodash'
import { OPEN, CLOSED } from './FacetActions'
import { FACETS_RECEIVED, TOGGLE_FACET, CLEAR_FACETS } from './FacetActions'

export const initialState = Immutable({
  allFacets: {},
  selectedFacets: {}
})

const facets = (state = initialState, action) => {
  switch(action.type) {
    case FACETS_RECEIVED:
      return Immutable.set(state, 'allFacets', action.metadata.facets)

    case TOGGLE_FACET:
      return Immutable.set(state, 'selectedFacets', action.selectedFacets)

    case CLEAR_FACETS:
      return initialState

    default:
      return state
  }
}

export default facets
