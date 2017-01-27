import Immutable from 'seamless-immutable'
import { SEARCH_COMPLETE, COUNT_HITS } from '../../actions/SearchActions'
import { FETCHED_GRANULES, CLEAR_GRANULES } from '../../actions/SearchActions'
import { FACETS_RECEIVED } from '../../search/facet/FacetActions'

export const initialState = Immutable({
  collections: {},
  granules: {},
  facets: {},
  totalCollections: 0
})

export const results = (state = initialState, action) => {
  switch(action.type) {

    case SEARCH_COMPLETE:
      let collections = {}
      action.items.forEach((val, key) => {
        collections[key] = val
      })
      return Immutable.set(state, 'collections', collections)

    case COUNT_HITS:
      return Immutable.set(state, 'totalCollections', action.totalHits)

    case FETCHED_GRANULES:
      const newGranules = action.granules.reduce(
          (existing, next) => existing.set(next.id, next.attributes), state.granules)
      return Immutable.set(state, 'granules', newGranules)

    case CLEAR_GRANULES:
      return Immutable.set(state, 'granules', initialState.granules)

    case FACETS_RECEIVED:
      return Immutable.set(state, 'facets', action.metadata.facets)

    default:
      return state
  }
}

export default results
