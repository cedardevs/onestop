import Immutable from 'seamless-immutable'
import { SEARCH_COMPLETE, COUNT_HITS, INCREMENT_COLLECTIONS_OFFSET, FETCHED_GRANULES,
  FACETS_RECEIVED, CLEAR_GRANULES, CLEAR_COLLECTIONS } from '../../actions/SearchRequestActions'

export const initialState = Immutable({
  collections: {},
  granules: {},
  facets: {},
  totalCollections: 0,
  collectionsPageOffset: 0
})

export const results = (state = initialState, action) => {
  switch(action.type) {

    case SEARCH_COMPLETE:
      let newCollections = {}
      action.items.forEach((val, key) => {
        newCollections[key] = val
      })
      let allCollections = state.collections.merge(newCollections)
      return Immutable.set(state, 'collections', allCollections)

    case CLEAR_COLLECTIONS:
      return Immutable.merge(state, {
        collections: initialState.collections,
        totalCollections: initialState.totalCollections
      })

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

    case INCREMENT_COLLECTIONS_OFFSET:
      return Immutable.set(state, 'collectionsPageOffset', state.collectionsPageOffset + 20)

    default:
      return state
  }
}

export default results
