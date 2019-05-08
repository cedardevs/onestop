import Immutable from 'seamless-immutable'
import {
  COLLECTION_CLEAR_FACETS,
  COLLECTION_REMOVE_FILTERS,
} from '../../actions/search/CollectionFilterActions'
import {
  // COLLECTION_SEARCH_SUCCESS,
  COLLECTION_DETAIL_SUCCESS,
  COLLECTION_SEARCH_COMPLETE,
  COLLECTION_SEARCH_ERROR,
  COLLECTION_DETAIL_COMPLETE,
  COLLECTION_DETAIL_ERROR,
} from '../../actions/search/CollectionRequestActions'
import {
  // COLLECTION_UPDATE_TOTAL,
  COLLECTION_INCREMENT_RESULTS_OFFSET,
  COLLECTION_CLEAR_RESULTS,
  COLLECTION_INCREMENT_DETAIL_GRANULES_RESULT_OFFSET,
  COLLECTION_CLEAR_DETAIL_GRANULES_RESULT, // TODO leaving these for the moment, because you can get total granules on a collection as aggregate data instead of the direct granule query, I think...
  COLLECTION_UPDATE_DETAIL_GRANULES_TOTAL,
  COLLECTION_METADATA_RECEIVED,
} from '../../actions/search/CollectionResultActions'

export const initialState = Immutable({
  collections: {},
  facets: {},
  totalCollections: 0,
  collectionsPageOffset: 0,
  totalGranules: 0,
  pageSize: 20,
  collectionDetail: null,
})

const collectionResults = (state, collections, action) => {
  // TODO rename this, it's vague
  return Immutable.merge(state, {
    // loadedCollections: (granules && Object.keys(granules).length) || 0, // TODO
    collections: collections,
    totalCollections: action.total,
    facets: action.metadata ? action.metadata.facets : initialState.facets,
  })
}

export const collectionResult = (state = initialState, action) => {
  switch (action.type) {
    // Result Effects from 'CollectionFilterActions'
    case COLLECTION_CLEAR_FACETS:
      return Immutable.set(state, 'facets', initialState.facets)

    case COLLECTION_REMOVE_FILTERS:
      return Immutable.set(state, 'facets', initialState.facets)

    // Result Effects from 'CollectionRequestActions'
    case COLLECTION_SEARCH_COMPLETE:
      let newCollections = action.items.reduce(
        (existing, next) => existing.set(next.id, next.attributes),
        initialState.collections
      )
      if (action.clearPreviousResults) {
        return collectionResults(state, newCollections, action)
      }

      let allCollections = state.collections.merge(newCollections)

      return collectionResults(state, allCollections, action)

    case COLLECTION_SEARCH_ERROR:
      return Immutable.merge(state, {
        // loadedCollections: initialState.loadedCollections, //TODO
        collections: initialState.collections,
        totalCollections: initialState.totalCollections,
        facets: initialState.facets,
      })

    // case COLLECTION_SEARCH_SUCCESS:
    //   let newCollections = {}
    //   action.items.forEach((val, key) => {
    //     newCollections[key] = val
    //   })
    //   let allCollections = state.collections.merge(newCollections)
    //   return Immutable.set(state, 'collections', allCollections)

    case COLLECTION_DETAIL_ERROR:
      return Immutable.set(state, 'collectionDetail', null) // TODO also set errors like with other new error actions
    case COLLECTION_DETAIL_SUCCESS:
      return Immutable.set(state, 'collectionDetail', action.result)

    // Result Effects from 'CollectionResultActions'
    case COLLECTION_CLEAR_RESULTS:
      return Immutable.merge(state, {
        collections: initialState.collections,
        totalCollections: initialState.totalCollections,
        collectionsPageOffset: initialState.collectionsPageOffset,
      })

    // case COLLECTION_UPDATE_TOTAL:
    //   return Immutable.set(state, 'totalCollections', action.totalCollections)

    case COLLECTION_CLEAR_DETAIL_GRANULES_RESULT:
      return Immutable.merge(state, {
        totalGranules: initialState.totalGranules,
      })

    case COLLECTION_UPDATE_DETAIL_GRANULES_TOTAL: // TODO when used?
      return Immutable.set(state, 'totalGranules', action.totalGranules)

    case COLLECTION_METADATA_RECEIVED: // TODO nuke this?
      return Immutable.set(state, 'facets', action.metadata.facets)

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
