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
      // Build the UI object we eventually expect to receive from the API
      let categories = {}
      _.forOwn(action.metadata.facets, (terms, category) => {
        categories[category] = {}
        for (let term of terms){
          categories[category][term.term] = {
            count: term.count,
            selected: false
          }
        }
      })
      // Update facets with previous checks or reset selected facets w/ new search
      if (action.processFacets){
        categories = Immutable.fromJS(categories).mergeDeep(state.get('selectedFacets'))
      } else {
        categories = Immutable.fromJS(categories)
        state = state.set('selectedFacets', initialState.get('selectedFacets'))
      }
      return state.set('allFacets', categories)

    case MODIFY_SELECTED_FACETS:
      return state.set('selectedFacets', action.selectedFacets)

    default:
      return state
  }
}

export default facets
