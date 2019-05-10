import Immutable from 'seamless-immutable'
import {
  COLLECTION_GET_DETAIL_COMPLETE,
  COLLECTION_GET_DETAIL_ERROR,
} from '../../actions/get/CollectionDetailRequestActions'

export const initialState = Immutable({
  collectionDetail: null,
})

export const collectionDetailResult = (state = initialState, action) => {
  switch (action.type) {
    case COLLECTION_GET_DETAIL_ERROR:
      return Immutable.set(state, 'collectionDetail', null) // TODO also set errors like with other new error actions
    case COLLECTION_GET_DETAIL_COMPLETE:
      return Immutable.set(state, 'collectionDetail', action.result)

    default:
      return state
  }
}

export default collectionDetailResult
