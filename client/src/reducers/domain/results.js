import Immutable from 'seamless-immutable'
import {
  SEARCH_COMPLETE, COUNT_HITS, INCREMENT_COLLECTIONS_OFFSET, CLEAR_COLLECTIONS,
  FETCHED_GRANULES, INCREMENT_GRANULES_OFFSET, CLEAR_GRANULES, COUNT_GRANULES,
  FACETS_RECEIVED
} from '../../actions/SearchRequestActions'

export const initialState = Immutable({
  collections: {},
  granules: {},
  facets: {},
  totalCollections: 0,
  collectionsPageOffset: 0,
  totalGranules: 0,
  granulesPageOffset: 0,
  pageSize: 20
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
        totalCollections: initialState.totalCollections,
        collectionsPageOffset: initialState.collectionsPageOffset
      })

    case COUNT_HITS:
      return Immutable.set(state, 'totalCollections', action.totalHits)

    case FETCHED_GRANULES:
      const newGranules = action.granules.reduce(
          (existing, next) => existing.set(next.id, next.attributes), state.granules)
      return Immutable.set(state, 'granules', newGranules)

    case CLEAR_GRANULES:
      return Immutable.merge(state, {
        granules: initialState.granules,
        totalGranules: initialState.totalGranules,
        granulesPageOffset: initialState.granulesPageOffset
      })

    case COUNT_GRANULES:
      return Immutable.set(state, 'totalGranules', action.totalGranules)

    case FACETS_RECEIVED:
      return Immutable.set(state, 'facets', action.metadata.facets)

    case INCREMENT_COLLECTIONS_OFFSET:
      return Immutable.set(state, 'collectionsPageOffset', state.collectionsPageOffset + state.pageSize)

    case INCREMENT_GRANULES_OFFSET:
      return Immutable.set(state, 'granulesPageOffset', state.granulesPageOffset + state.pageSize)

    default:
      return state
  }
}

export default results
