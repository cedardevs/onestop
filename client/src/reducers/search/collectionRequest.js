import Immutable from 'seamless-immutable'
import {
  COLLECTION_SEARCH_REQUEST,
  COLLECTION_SEARCH_SUCCESS,
  COLLECTION_DETAIL_REQUEST,
  COLLECTION_DETAIL_SUCCESS,
} from '../../actions/search/CollectionRequestActions'

export const initialState = Immutable({
  collectionSearchRequestInFlight: false,
  collectionDetailRequestInFlight: false,
})

export const collectionRequest = (state = initialState, action) => {
  switch (action.type) {
    case COLLECTION_SEARCH_REQUEST:
      return Immutable.set(state, 'collectionSearchRequestInFlight', true)

    case COLLECTION_SEARCH_SUCCESS:
      return Immutable.set(state, 'collectionSearchRequestInFlight', false)

    case COLLECTION_DETAIL_REQUEST:
      return Immutable.set(state, 'collectionDetailRequestInFlight', action.id)

    case COLLECTION_DETAIL_SUCCESS:
      return Immutable.set(state, 'collectionDetailRequestInFlight', false)

    default:
      return state
  }
}

export default collectionRequest
