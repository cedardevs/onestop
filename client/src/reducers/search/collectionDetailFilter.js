import Immutable from 'seamless-immutable'
import {COLLECTION_DETAIL_REQUESTED} from '../../actions/routing/CollectionDetailStateActions'

export const initialState = Immutable({
  // queryText: '', Not currently a valid param for granule searchs!
  geoJSON: null,
  startDateTime: null,
  endDateTime: null,
  selectedFacets: {},
  selectedIds: [],
  excludeGlobal: null,
})

export const collectionDetailFilter = (state = initialState, action) => {
  switch (action.type) {
    case COLLECTION_DETAIL_REQUESTED:
      return Immutable.without(
        Immutable.merge(initialState, [
          action.filters,
          {
            selectedIds: [ action.id ],
          },
        ]),
        (value, key) => !(key in initialState)
      )
      return updateFilters(
        Immutable.merge(initialState, [
          action.filters,
          {
            selectedIds: [ action.id ],
          },
        ])
      )

    default:
      return state
  }
}

export default collectionDetailFilter
