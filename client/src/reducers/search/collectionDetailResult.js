import Immutable from 'seamless-immutable'
import {
  COLLECTION_GET_DETAIL_COMPLETE,
  COLLECTION_GET_DETAIL_ERROR,
} from '../../actions/routing/CollectionDetailStateActions'

export const initialState = Immutable({
  collection: null,
  totalGranuleCount: 0,
})

export const collectionDetailResult = (state = initialState, action) => {
  switch (action.type) {
    case COLLECTION_GET_DETAIL_ERROR:
      return Immutable.merge(state, initialState)
    case COLLECTION_GET_DETAIL_COMPLETE:
      return Immutable.merge(state, {
        collection: action.collection,
        totalGranuleCount: action.totalGranuleCount,
      })

    default:
      return state
  }
}

export default collectionDetailResult
