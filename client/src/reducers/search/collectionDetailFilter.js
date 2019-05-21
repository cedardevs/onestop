import Immutable from 'seamless-immutable'
import {COLLECTION_DETAIL_REQUESTED} from '../../actions/routing/CollectionDetailStateActions'

// TODO add testing!

export const initialState = Immutable({
  // queryText: '', Not currently a valid param for granule searchs!
  geoJSON: null,
  startDateTime: null,
  endDateTime: null,
  selectedFacets: {},
  selectedIds: [],
  excludeGlobal: null,
  pageOffset: 0,
})

const updateFilters = ({
  geoJSON,
  startDateTime,
  endDateTime,
  selectedFacets,
  selectedIds,
  excludeGlobal,
}) => {
  return Immutable.merge(initialState, {
    geoJSON: geoJSON || initialState.geoJSON,
    startDateTime: startDateTime || initialState.startDateTime,
    endDateTime: endDateTime || initialState.endDateTime,
    selectedFacets: selectedFacets || initialState.selectedFacets,
    selectedIds: selectedIds || initialState.selectedIds,
    excludeGlobal: excludeGlobal || initialState.excludeGlobal,
  })
}

export const collectionDetailFilter = (state = initialState, action) => {
  switch (action.type) {
    case COLLECTION_DETAIL_REQUESTED:
      // return Immutable.merge(state, [action.filters, {pageOffset: initialState.pageOffset,
      // selectedIds: [ action.id ],}])
      return updateFilters(
        Immutable.merge(initialState, [
          action.filters,
          {
            pageOffset: initialState.pageOffset,
            selectedIds: [ action.id ],
          },
        ])
      ) // TODO this is now an even gnarlier, hard to understand action. Basically merge inital state + all the input  / side effects together, then run thru updateFilters function to remove state which doesn't exist (queryText)

    default:
      return state
  }
}

export default collectionDetailFilter
