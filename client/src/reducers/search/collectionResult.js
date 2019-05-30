import Immutable from 'seamless-immutable'
import {
  COLLECTION_NEW_SEARCH_RESULTS_RECEIVED,
  COLLECTION_MORE_RESULTS_RECEIVED,
  COLLECTION_SEARCH_ERROR,
} from '../../actions/routing/CollectionSearchStateActions'

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

const newSearchResultsReceived = (state, total, collections, facets) => {
  return Immutable.merge(state, {
    loadedCollectionCount:
      (collections && Object.keys(collections).length) || 0,
    collections: collections,
    totalCollectionCount: total,
    facets: facets,
  })
}

const moreResultsReceived = (state, newCollections) => {
  let collections = state.collections.merge(newCollections)

  return Immutable.merge(state, {
    loadedCollectionCount:
      (collections && Object.keys(collections).length) || 0,
    collections: collections,
  })
}

export const collectionResult = (state = initialState, action) => {
  switch (action.type) {
    case COLLECTION_NEW_SEARCH_RESULTS_RECEIVED:
      return newSearchResultsReceived(
        state,
        action.total,
        getCollectionsFromAction(action),
        action.facets
      )

    case COLLECTION_MORE_RESULTS_RECEIVED:
      return moreResultsReceived(state, getCollectionsFromAction(action))

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
