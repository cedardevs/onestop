import Immutable from 'immutable';
import _ from 'lodash'
import { OPEN, CLOSED } from './FacetActions'
import { FACETS_RECEIVED, UPDATE_FACETS } from './FacetActions'

export const initialState = Immutable.fromJS({
  categories: null,
  facetsSelected: Immutable.Map()
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
      // Update facets with previous checks
      if (action.processFacets){
        categories = Immutable.fromJS(categories).mergeDeep(state.get('facetsSelected'))
      } else {
        categories = Immutable.fromJS(categories)
      }
      return state.set('categories', categories)

    case UPDATE_FACETS:
      return state.set('facetsSelected', action.facetsSelected)
    default:
      return state
  }
}

export default facets
