import Immutable from 'seamless-immutable'
import {
  // COLLECTION_SEARCH_REQUEST,
  // COLLECTION_SEARCH_SUCCESS,
  // COLLECTION_DETAIL_REQUEST,
  // COLLECTION_DETAIL_SUCCESS,
  COLLECTION_SEARCH_START,
  COLLECTION_SEARCH_COMPLETE,
  COLLECTION_SEARCH_ERROR,
  COLLECTION_DETAIL_START,
  COLLECTION_DETAIL_COMPLETE,
  COLLECTION_DETAIL_ERROR,
} from '../../actions/search/CollectionRequestActions'

export const initialState = Immutable({
  collectionSearchRequestInFlight: false,
  collectionDetailRequestInFlight: false,
})

export const collectionRequest = (state = initialState, action) => {
  switch (action.type) {
    case COLLECTION_SEARCH_START:
      return Immutable.set(state, 'collectionSearchRequestInFlight', true)

    case COLLECTION_SEARCH_COMPLETE:
      return Immutable.set(state, 'collectionSearchRequestInFlight', false)

    case COLLECTION_SEARCH_ERROR:
      return Immutable.set(state, 'collectionSearchRequestInFlight', false)

    case COLLECTION_DETAIL_START:
      return Immutable.set(state, 'collectionDetailRequestInFlight', true)

    case COLLECTION_DETAIL_COMPLETE:
      return Immutable.set(state, 'collectionDetailRequestInFlight', false)

    case COLLECTION_DETAIL_ERROR:
      return Immutable.set(state, 'collectionDetailRequestInFlight', false)

    // case COLLECTION_SEARCH_REQUEST:
    //   return Immutable.set(state, 'collectionSearchRequestInFlight', true)
    //
    // case COLLECTION_SEARCH_SUCCESS:
    //   return Immutable.set(state, 'collectionSearchRequestInFlight', false)
    //
    // case COLLECTION_DETAIL_REQUEST:
    //   return Immutable.set(state, 'collectionDetailRequestInFlight', action.id)
    //
    // case COLLECTION_DETAIL_SUCCESS:
    //   return Immutable.set(state, 'collectionDetailRequestInFlight', false)

    default:
      return state
  }
}

export default collectionRequest
