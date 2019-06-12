import Immutable from 'seamless-immutable'
import {COLLECTION_DETAIL_REQUESTED} from '../../actions/routing/CollectionDetailStateActions'

export const initialState = Immutable({
  // queryText: '', Not currently a valid param for granule searchs!
  geoJSON: null,
  startDateTime: null,
  endDateTime: null,
  selectedFacets: {},
  selectedCollectionIds: [],
  excludeGlobal: null,
})

const updateFilters = (collectionId, filters) => {
  // create state with selectedCollectionIds set explicitly to collectionId,
  // and all other filters set by action (or default to initial state values)
  return Immutable.merge(initialState, [
    filters,
    {
      selectedCollectionIds: [ collectionId ],
    },
  ])
}

const stripExtraFields = state => {
  // without strips out all fields not part of original state, so no new keys can be added:
  return Immutable.without(state, (value, key) => !(key in initialState))
}

const newDetailRequest = (collectionId, filters) => {
  return stripExtraFields(updateFilters(collectionId, filters))
}

export const collectionDetailFilter = (state = initialState, action) => {
  switch (action.type) {
    case COLLECTION_DETAIL_REQUESTED:
      return newDetailRequest(action.id, action.filters)

    default:
      return state
  }
}

export default collectionDetailFilter
