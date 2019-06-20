import Immutable from 'seamless-immutable'
import {
  COLLECTION_DETAIL_REQUESTED,
  COLLECTION_DETAIL_RECEIVED,
  COLLECTION_DETAIL_ERROR,
  GRANULE_MATCHING_COUNT_REQUESTED,
  GRANULE_MATCHING_COUNT_RECEIVED,
  GRANULE_MATCHING_COUNT_ERROR,
} from '../../actions/routing/CollectionDetailStateActions'

export const initialState = Immutable({
  inFlight: false,
  requestedID: null,
  errorMessage: '',
  backgroundInFlight: false, // arguably these should have a first class place on a separate piece of state, rather than being labelled 'background'. As long as only one background request is needed for this view, this is fine for now. To be fair, background label makes sense considering we shouldn't show loading messages based on it...
  backgroundErrorMessage: '',
})

export const collectionDetailRequest = (state = initialState, action) => {
  switch (action.type) {
    case COLLECTION_DETAIL_REQUESTED:
      return Immutable.merge(state, {
        inFlight: true,
        requestedID: action.id,
        errorMessage: '',
      })

    case GRANULE_MATCHING_COUNT_REQUESTED:
      return Immutable.merge(state, {
        backgroundInFlight: true,
        backgroundErrorMessage: '',
      })

    case COLLECTION_DETAIL_RECEIVED:
      return Immutable.merge(state, {
        inFlight: false,
        requestedID: null,
      })

    case GRANULE_MATCHING_COUNT_RECEIVED:
      return Immutable.merge(state, {
        backgroundInFlight: false,
      })

    case COLLECTION_DETAIL_ERROR:
      return Immutable.merge(state, {
        inFlight: false,
        errorMessage: action.errors,
      })

    case GRANULE_MATCHING_COUNT_ERROR:
      return Immutable.merge(state, {
        backgroundInFlight: false,
        backgroundErrorMessage: action.errors,
      })

    default:
      return state
  }
}

export default collectionDetailRequest
