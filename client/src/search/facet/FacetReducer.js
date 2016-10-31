import Immutable from 'immutable';
import _ from 'lodash'
import { OPEN, CLOSED } from './FacetActions'
import { FACETS_RECEIVED, MODIFY_SELECTED_FACETS, CLEAR_FACETS } from './FacetActions'

export const initialState = Immutable.fromJS({
  allFacets: null,
  selectedFacets: {}
})

const facets = (state = initialState, action) => {
  switch(action.type) {
    case FACETS_RECEIVED:
      // Update facets with previous checks
      let categories = Immutable.fromJS(action.metadata.facets).mergeDeep(state.get('selectedFacets'))
      return state.set('allFacets', categories)

    case MODIFY_SELECTED_FACETS:
      // Receives an already immutable object from upstream
      const selectedFacets = !_.isEmpty(action.selectedFacets) ?
          Immutable.fromJS(action.selectedFacets) : initialState.selectedFacets
      return state.set('selectedFacets', selectedFacets)

    case CLEAR_FACETS:
      return initialState

    default:
      return state
  }
}

export default facets
