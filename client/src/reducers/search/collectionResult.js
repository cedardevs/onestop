import Immutable from 'seamless-immutable'
// import _ from 'lodash'
import {
  COLLECTION_NEW_SEARCH_REQUESTED,
  COLLECTION_MORE_RESULTS_REQUESTED,
  COLLECTION_NEW_SEARCH_RESULTS_RECIEVED,
  COLLECTION_MORE_RESULTS_RECIEVED,
  COLLECTION_SEARCH_ERROR,
} from '../../actions/search/CollectionRequestActions'
// import {
//   COLLECTION_CLEAR_RESULTS,
// } from '../../actions/search/CollectionResultActions'

export const initialState = Immutable({
  collections: {},
  facets: {},
  totalCollections: 0,
  loadedCollections: 0, // TODO rename to loadedCount or numberLoaded or something?
  collectionsPageOffset: 0, // TODO This would make more logical sense on request, probably...
  // totalGranules: 0, // TODO when is this still used??
  pageSize: 20, // TODO when is this changed? make sure granule page size can be changed, if needed?
})

const getCollectionsFromAction = action => {
  /* TODO make sure all of this stuff ... is the same as the reduce used below
  const result = _.reduce(
    action.items,
    (map, resource) => {
      return map.set(
        resource.id,
        _.assign({type: resource.type}, resource.attributes)
      )
    },
    new Map()
  )
  let newCollections = {}
    result.forEach((val, key) => {
      newCollections[key] = val
    })
    */

  return action.items.reduce(
    (existing, next) => existing.set(next.id, next.attributes),
    initialState.collections
  )
}

const newSearchResultsRecieved = (state, total, collections, facets) => {
  return Immutable.merge(state, {
    loadedCollections: (collections && Object.keys(collections).length) || 0,
    collections: collections,
    totalCollections: total,
    facets: facets,
  })
}

const moreResultsRecieved = (state, newCollections) => {
  let collections = state.collections.merge(newCollections)

  return Immutable.merge(state, {
    loadedCollections: (collections && Object.keys(collections).length) || 0,
    collections: collections,
  })
}

export const collectionResult = (state = initialState, action) => {
  switch (action.type) {
    // case COLLECTION_CLEAR_RESULTS: // TODO full reset, facets and all? if not why not?
    //   return Immutable.merge(state, {
    //     collections: initialState.collections,
    //     totalCollections: initialState.totalCollections,
    //     loadedCollections: initialState.loadedCollections,
    //     collectionsPageOffset: initialState.collectionsPageOffset,
    //   })

    case COLLECTION_NEW_SEARCH_RESULTS_RECIEVED:
      return newSearchResultsRecieved(
        state,
        action.total,
        getCollectionsFromAction(action),
        action.metadata.facets
      )

    case COLLECTION_MORE_RESULTS_RECIEVED:
      return moreResultsRecieved(state, getCollectionsFromAction(action))

    case COLLECTION_SEARCH_ERROR:
      return Immutable.merge(state, {
        loadedCollections: initialState.loadedCollections,
        collections: initialState.collections,
        totalCollections: initialState.totalCollections,
        facets: initialState.facets,
      })

    case COLLECTION_NEW_SEARCH_REQUESTED:
      return Immutable.set(
        state,
        'collectionsPageOffset',
        initialState.collectionsPageOffset
      )

    case COLLECTION_MORE_RESULTS_REQUESTED:
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
