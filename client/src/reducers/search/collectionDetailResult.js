import Immutable from 'seamless-immutable'
import {
  COLLECTION_DETAIL_RECEIVED,
  COLLECTION_DETAIL_ERROR,
} from '../../actions/routing/CollectionDetailStateActions'

export const initialState = Immutable({
  collection: null,
  totalGranuleCount: 0,
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

    default:
      return state
  }
}

export default collectionDetailResult
