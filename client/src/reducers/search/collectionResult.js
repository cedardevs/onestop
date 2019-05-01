import Immutable from 'seamless-immutable'
import {
  COLLECTION_CLEAR_FACETS,
  COLLECTION_REMOVE_FILTERS,
} from '../../actions/search/CollectionFilterActions'
import {
  COLLECTION_SEARCH_SUCCESS,
  COLLECTION_DETAIL_SUCCESS,
  COLLECTION_DETAIL_GRANULES_SUCCESS,
} from '../../actions/search/CollectionRequestActions'
import {
  COLLECTION_UPDATE_TOTAL,
  COLLECTION_INCREMENT_RESULTS_OFFSET,
  COLLECTION_CLEAR_RESULTS,
  COLLECTION_INCREMENT_DETAIL_GRANULES_RESULT_OFFSET,
  COLLECTION_CLEAR_DETAIL_GRANULES_RESULT,
  COLLECTION_UPDATE_DETAIL_GRANULES_TOTAL,
  COLLECTION_METADATA_RECEIVED,
} from '../../actions/search/CollectionResultActions'

export const initialState = Immutable({
  collections: {},
  granules: {},
  facets: {},
  totalCollections: 0,
  collectionsPageOffset: 0,
  totalGranules: 0,
  granulesPageOffset: 0,
  pageSize: 20,
  collectionDetail: null,
})

export const collectionResult = (state = initialState, action) => {
  switch (action.type) {
    // Result Effects from 'CollectionFilterActions'
    case COLLECTION_CLEAR_FACETS:
      return Immutable.set(state, 'facets', initialState.facets)

    case COLLECTION_REMOVE_FILTERS:
      return Immutable.set(state, 'facets', initialState.facets)

    // Result Effects from 'CollectionRequestActions'
    case COLLECTION_SEARCH_SUCCESS:
      let newCollections = {}
      action.items.forEach((val, key) => {
        newCollections[key] = val
      })
      let allCollections = state.collections.merge(newCollections)
      return Immutable.set(state, 'collections', allCollections)

    case COLLECTION_DETAIL_SUCCESS:
      return Immutable.set(state, 'collectionDetail', action.result)

    case COLLECTION_DETAIL_GRANULES_SUCCESS:
      const newGranules = action.granules.reduce(
        (existing, next) => existing.set(next.id, next.attributes),
        state.granules
      )
      return Immutable.set(state, 'granules', newGranules)

    // Result Effects from 'CollectionResultActions'
    case COLLECTION_CLEAR_RESULTS:
      return Immutable.merge(state, {
        collections: initialState.collections,
        totalCollections: initialState.totalCollections,
        collectionsPageOffset: initialState.collectionsPageOffset,
      })

    case COLLECTION_UPDATE_TOTAL:
      return Immutable.set(state, 'totalCollections', action.totalCollections)

    case COLLECTION_CLEAR_DETAIL_GRANULES_RESULT:
      return Immutable.merge(state, {
        granules: initialState.granules,
        totalGranules: initialState.totalGranules,
        granulesPageOffset: initialState.granulesPageOffset,
      })

    case COLLECTION_UPDATE_DETAIL_GRANULES_TOTAL:
      return Immutable.set(state, 'totalGranules', action.totalGranules)

    case COLLECTION_METADATA_RECEIVED:
      return Immutable.set(state, 'facets', action.metadata.facets)

    case COLLECTION_INCREMENT_RESULTS_OFFSET:
      return Immutable.set(
        state,
        'collectionsPageOffset',
        state.collectionsPageOffset + state.pageSize
      )

    case COLLECTION_INCREMENT_DETAIL_GRANULES_RESULT_OFFSET:
      return Immutable.set(
        state,
        'granulesPageOffset',
        state.granulesPageOffset + state.pageSize
      )

    default:
      return state
  }
}

export default collectionResult
