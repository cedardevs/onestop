import Immutable from 'seamless-immutable'
import {
  COLLECTION_GET_DETAIL_COMPLETE,
  COLLECTION_GET_DETAIL_ERROR,
} from '../../actions/get/CollectionDetailRequestActions'
import {
  COLLECTION_CLEAR_DETAIL_GRANULES_RESULT, // TODO leaving these for the moment, because you can get total granules on a collection as aggregate data instead of the direct granule query, I think...
} from '../../actions/search/CollectionResultActions'

export const initialState = Immutable({
  // collections: {},
  // facets: {},
  // totalCollections: 0,
  // collectionsPageOffset: 0,
  // totalGranules: 0,
  // pageSize: 20,
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
