import Immutable from 'seamless-immutable'
import {
  COLLECTION_DETAIL_REQUESTED,
  COLLECTION_DETAIL_RECEIVED,
  COLLECTION_DETAIL_ERROR,
  GRANULE_MATCHING_COUNT_RECEIVED,
} from '../../actions/routing/CollectionDetailStateActions'

export const initialState = Immutable({
  collection: null,
  totalGranuleCount: 0,
  filteredGranuleCount: 0,
})

export const collectionDetailResult = (state = initialState, action) => {
  switch (action.type) {
    case COLLECTION_DETAIL_ERROR:
      return Immutable.merge(state, initialState)
    case COLLECTION_DETAIL_RECEIVED:
      return Immutable.merge(state, {
        collection: action.collection,
        totalGranuleCount: action.totalGranuleCount,
      })
    case COLLECTION_DETAIL_REQUESTED:
      return Immutable.merge(state, {
        filteredGranuleCount: initialState.filteredGranuleCount, // reset when we have a new request, before background results return (just in case there's a problem with that request, the UI can display regardless)  // TODO just use the backgroundInFlight and default it to zero instead. Duh.
      })
    case GRANULE_MATCHING_COUNT_RECEIVED:
      return Immutable.merge(state, {
        filteredGranuleCount: action.filteredGranuleCount,
      })

    default:
      return state
  }
}

export default collectionDetailResult
