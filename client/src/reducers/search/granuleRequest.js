import Immutable from 'seamless-immutable'
import {
  GRANULE_SEARCH_REQUEST,
  GRANULE_SEARCH_SUCCESS,
  // GRANULE_DETAIL_REQUEST,
  // GRANULE_DETAIL_SUCCESS,
  // GRANULE_DETAIL_GRANULES_REQUEST,
  // GRANULE_DETAIL_GRANULES_SUCCESS,
} from '../../actions/search/GranuleRequestActions'

export const initialState = Immutable({
  granuleSearchRequestInFlight: false,
  // granuleDetailRequestInFlight: false,
  // granuleDetailGranulesRequestInFlight: false,
})

export const granuleRequest = (state = initialState, action) => {
  switch (action.type) {
    case GRANULE_SEARCH_REQUEST:
      return Immutable.set(state, 'granuleSearchRequestInFlight', true)

    case GRANULE_SEARCH_SUCCESS:
      return Immutable.set(state, 'granuleSearchRequestInFlight', false)

    // case GRANULE_DETAIL_REQUEST:
    //   return Immutable.set(state, 'granuleDetailRequestInFlight', action.id)
    //
    // case GRANULE_DETAIL_SUCCESS:
    //   return Immutable.set(state, 'granuleDetailRequestInFlight', false)
    //
    // case GRANULE_DETAIL_GRANULES_REQUEST:
    //   return Immutable.set(
    //     state,
    //     'granuleDetailGranulesRequestInFlight',
    //     true
    //   )
    //
    // case GRANULE_DETAIL_GRANULES_SUCCESS:
    //   return Immutable.set(
    //     state,
    //     'granuleDetailGranulesRequestInFlight',
    //     false
    //   )

    default:
      return state
  }
}

export default granuleRequest
