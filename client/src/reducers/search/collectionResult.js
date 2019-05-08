import Immutable from 'seamless-immutable'
import {COLLECTION_REMOVE_FILTERS} from '../../actions/search/CollectionFilterActions'
import {
  COLLECTION_SEARCH_COMPLETE,
  COLLECTION_SEARCH_ERROR,
} from '../../actions/search/CollectionRequestActions'
import {
  COLLECTION_INCREMENT_RESULTS_OFFSET,
  COLLECTION_CLEAR_RESULTS,
} from '../../actions/search/CollectionResultActions'

export const initialState = Immutable({
  collections: {},
  facets: {},
  totalCollections: 0,
  loadedCollections: 0, // TODO rename to loadedCount or numberLoaded or something?
  collectionsPageOffset: 0,
  totalGranules: 0, // TODO when is this still used??
  pageSize: 20,
})

const collectionResults = (state, collections, action) => {
  // TODO rename this, it's vague
  return Immutable.merge(state, {
    loadedCollections: (collections && Object.keys(collections).length) || 0,
    collections: collections,
    totalCollections: action.total,
    facets: action.metadata ? action.metadata.facets : initialState.facets,
  })
}

export const collectionResult = (state = initialState, action) => {
  switch (action.type) {
    // Result Effects from 'CollectionFilterActions'

    case COLLECTION_REMOVE_FILTERS:
      return Immutable.set(state, 'facets', initialState.facets)

    // Result Effects from 'CollectionRequestActions'
    case COLLECTION_SEARCH_COMPLETE:
      let newCollections = {}
      action.items.forEach((val, key) => {
        newCollections[key] = val
      })
      if (action.clearPreviousResults) {
        return collectionResults(state, newCollections, action)
      }

      let allCollections = state.collections.merge(newCollections)

      return collectionResults(state, allCollections, action)

    case COLLECTION_SEARCH_ERROR:
      return Immutable.merge(state, {
        loadedCollections: initialState.loadedCollections,
        collections: initialState.collections,
        totalCollections: initialState.totalCollections,
        facets: initialState.facets,
      })

    // Result Effects from 'CollectionResultActions'
    case COLLECTION_CLEAR_RESULTS:
      return Immutable.merge(state, {
        collections: initialState.collections,
        totalCollections: initialState.totalCollections,
        collectionsPageOffset: initialState.collectionsPageOffset,
      })

    // case COLLECTION_UPDATE_TOTAL:
    //   return Immutable.set(state, 'totalCollections', action.totalCollections)

    case COLLECTION_INCREMENT_RESULTS_OFFSET:
      return Immutable.set(
        state,
        'collectionsPageOffset',
        state.collectionsPageOffset + state.pageSize
      )

    default:
      return state
  }
}

export default collectionResult
