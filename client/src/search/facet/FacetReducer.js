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
      let categories = action.metadata.facets
      // Update facets with previous checks or reset selected facets w/ new search
      if (action.processFacets){
        categories = Immutable.fromJS(categories).mergeDeep(state.get('selectedFacets'))
      } else {
        categories = Immutable.fromJS(categories)
        state = state.set('selectedFacets', initialState.get('selectedFacets'))
      }
      return state.set('allFacets', categories)

    case MODIFY_SELECTED_FACETS:
      return state.set('selectedFacets', (action.selectedFacets ?
        action.selectedFacets : initialState.selectedFacets))

    default:
      return state
  }
}

export default facets
