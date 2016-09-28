import Immutable from 'immutable';
import _ from 'lodash'
import { OPEN, CLOSED } from './FacetActions'
import { FACETS_RECEIVED, MODIFY_SELECTED_FACETS } from './FacetActions'

export const initialState = Immutable.fromJS({
  allFacets: null,
  selectedFacets: Immutable.Map()
})

const facets = (state = initialState, action) => {
  switch(action.type) {
    case FACETS_RECEIVED:
      let categories = Immutable.fromJS(action.metadata.facets)
      // Update facets with previous checks or reset selected facets w/ new search
      if (action.procSelectedFacets){
        categories = categories.mergeDeep(state.get('selectedFacets'))
      } else {
        // Reset selected facets to original state
        state = state.set('selectedFacets', initialState.get('selectedFacets'))
      }
      return state.set('allFacets', categories)

    case MODIFY_SELECTED_FACETS:
      // Receives an already immutable object from upstream
      const selectedFacets = !_.isEmpty(action.selectedFacets) ?
        Immutable.fromJS(action.selectedFacets) : initialState.selectedFacets
      return state.set('selectedFacets', selectedFacets)

    default:
      return state
  }
}

export default facets
