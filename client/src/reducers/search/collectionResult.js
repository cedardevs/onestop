import Immutable from 'seamless-immutable'
// import _ from 'lodash'
import {
  COLLECTION_NEW_SEARCH_RESULTS_RECIEVED,
  COLLECTION_MORE_RESULTS_RECIEVED,
  COLLECTION_SEARCH_ERROR,
} from '../../actions/routing/CollectionSearchStateActions'
// import {
//   COLLECTION_CLEAR_RESULTS,
// } from '../../actions/search/CollectionResultActions'

export const initialState = Immutable({
  collections: {},
  facets: {},
  totalCollectionCount: 0,
  loadedCollectionCount: 0,
})

const getCollectionsFromAction = action => {
  return action.items.reduce(
    (existing, next) => existing.set(next.id, next.attributes),
    initialState.collections
  )
}

const newSearchResultsRecieved = (state, total, collections, facets) => {
  return Immutable.merge(state, {
    loadedCollectionCount:
      (collections && Object.keys(collections).length) || 0,
    collections: collections,
    totalCollectionCount: total,
    facets: facets,
  })
}

const moreResultsRecieved = (state, newCollections) => {
  let collections = state.collections.merge(newCollections)

  return Immutable.merge(state, {
    loadedCollectionCount:
      (collections && Object.keys(collections).length) || 0,
    collections: collections,
  })
}

export const collectionResult = (state = initialState, action) => {
  switch (action.type) {
    // case COLLECTION_CLEAR_RESULTS: // TODO full reset, facets and all? if not why not?
    //   return Immutable.merge(state, {
    //     collections: initialState.collections,
    //     totalCollectionCount: initialState.totalCollectionCount,
    //     loadedCollectionCount: initialState.loadedCollectionCount,
    //   })

    case COLLECTION_NEW_SEARCH_RESULTS_RECIEVED:
      return newSearchResultsRecieved(
        state,
        action.total,
        getCollectionsFromAction(action),
        action.facets
      )

    case COLLECTION_MORE_RESULTS_RECIEVED:
      return moreResultsRecieved(state, getCollectionsFromAction(action))

    case COLLECTION_SEARCH_ERROR:
      return Immutable.merge(state, {
        loadedCollectionCount: initialState.loadedCollectionCount,
        collections: initialState.collections,
        totalCollectionCount: initialState.totalCollectionCount,
        facets: initialState.facets,
      })

    default:
      return state
  }
}

export default collectionResult
