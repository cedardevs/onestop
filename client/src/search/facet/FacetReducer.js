import Immutable from 'immutable';
import _ from 'lodash'
import { OPEN, CLOSED } from './FacetActions'
import { FACETS_RECEIVED, UPDATE_FACETS } from './FacetActions'

export const initialState = Immutable.fromJS({
  categories: null
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
      return state.set('categories', Immutable.fromJS(categories))

    case UPDATE_FACETS:
      const { name, value, selected } = action.facet
      return state.setIn(['categories', name, value, 'selected'], selected)
    default:
      return state
  }
}

export default facets
