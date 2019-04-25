import Immutable from 'seamless-immutable'

export const initialState = Immutable({
  queryText: '',
  geoJSON: null,
  startDateTime: null,
  endDateTime: null,
  selectedFacets: {},
  selectedIds: [],
  excludeGlobal: null,
})

export const granuleFilter = (state = initialState, action) => {
  switch (action.type) {
    // TODO: create granule filter actions
    default:
      return state
  }
}

export default granuleFilter
