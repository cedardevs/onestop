import _ from 'lodash'
import {fetchCollectionSearch} from './AsyncHelpers'
import {showErrors} from '../ErrorActions'

import {
  assembleSearchRequest,
  encodeLocationDescriptor,
  // decodeQueryString,
} from '../../utils/queryUtils'
import {ROUTE, isPathNew} from '../../utils/urlUtils'
import {
  collectionNewSearchRequested,
  collectionNewSearchResetFiltersRequested,
  collectionMoreResultsRequested,
  collectionNewSearchResultsReceived,
  collectionMoreResultsReceived,
  collectionSearchError,
  collectionUpdateQueryText,
} from './CollectionSearchStateActions'
import {
  collectionFilter,
  initialState,
} from '../../reducers/search/collectionFilter'

const getFilterFromState = state => {
  return (state && state.search && state.search.collectionFilter) || {}
}

const isRequestInvalid = state => {
  const inFlight = state.search.collectionRequest.inFlight
  return inFlight
}

// const errorHandler = (dispatch, e) => {
//   // dispatch(showErrors(e.errors || e)) // TODO show errors
//   dispatch(collectionSearchError(e.errors || e))
// }
//

const collectionBodyBuilder = (filterState, requestFacets) => {
  const body = assembleSearchRequest(filterState, requestFacets)
  const hasQueries = body && body.queries && body.queries.length > 0
  const hasFilters = body && body.filters && body.filters.length > 0
  if (!(hasQueries || hasFilters)) {
    return undefined
  }
  return body
}

// const newSearchSuccessHandler = (dispatch, payload) => {
//   dispatch(
//     collectionNewSearchResultsReceived(
//       payload.meta.total,
//       payload.data,
//       payload.meta.facets
//     )
//   )
// }

export const submitCollectionSearchWithQueryText = (history, queryText) => {
  const filterState = collectionFilter(
    initialState,
    collectionUpdateQueryText(queryText) // TODO ew stop abusing reducers..
  )

  return submitCollectionSearchWithFilter(history, filterState)
}

export const submitCollectionSearchWithFilter = (history, filterState) => {
  // note: this updates the URL as well, it is not intended to be just a background search - make a new action if we need that case handled

  return async (dispatch, getState) => {
    if (isRequestInvalid(getState())) {
      return // TODO test this!
    }
    dispatch(collectionNewSearchResetFiltersRequested(filterState))
    const updatedFilterState = getFilterFromState(getState())
    navigateToCollectionUrl(history, updatedFilterState)

    const body = collectionBodyBuilder(updatedFilterState, true)
    if (!body) {
      dispatch(collectionSearchError('Invalid Request'))
      return
    }
    return fetchCollectionSearch(
      body,
      payload => {
        dispatch(
          collectionNewSearchResultsReceived(
            payload.meta.total,
            payload.data,
            payload.meta.facets
          )
        )
      },
      e => {
        // dispatch(showErrors(e.errors || e)) // TODO
        dispatch(collectionSearchError(e.errors || e))
      }
    )
  }
}

//
//
//   const prefetchHandler = dispatch => {
//     dispatch(collectionNewSearchResetFiltersRequested(filterState))
//     dispatch(navigateToCollectionUrl(history, filterState))
//   }
//
//   const bodyBuilder = () => {
//     return collectionBodyBuilder(filterState, true)
//   }
//
//   return buildSearchAction(
//     'collection',
//     isRequestInvalid,
//     prefetchHandler,
//     bodyBuilder,
//     newSearchSuccessHandler,
//     errorHandler
//   )
// }

export const submitCollectionSearch = history => {
  // note: this updates the URL as well, it is not intended to be just a background search - make a new action if we need that case handled

  return async (dispatch, getState) => {
    if (isRequestInvalid(getState())) {
      return // TODO test this!
    }
    dispatch(collectionNewSearchRequested())
    const updatedFilterState = getFilterFromState(getState())
    navigateToCollectionUrl(history, updatedFilterState)

    const body = collectionBodyBuilder(updatedFilterState, true)
    if (!body) {
      dispatch(collectionSearchError('Invalid Request'))
      return
    }
    return fetchCollectionSearch(
      body,
      payload => {
        dispatch(
          collectionNewSearchResultsReceived(
            payload.meta.total,
            payload.data,
            payload.meta.facets
          )
        )
      },
      e => {
        // dispatch(showErrors(e.errors || e)) // TODO
        dispatch(collectionSearchError(e.errors || e))
      }
    )
  }
  //
  // const prefetchHandler = (dispatch, state) => {
  //   const filterState = getFilterFromState(state)
  //   dispatch(collectionNewSearchRequested())
  //   dispatch(navigateToCollectionUrl(history, filterState))
  // }
  //
  // const bodyBuilder = state => {
  //   return collectionBodyBuilder(getFilterFromState(state), true)
  // }
  //
  // return buildSearchAction(
  //   'collection',
  //   isRequestInvalid,
  //   prefetchHandler,
  //   bodyBuilder,
  //   newSearchSuccessHandler,
  //   errorHandler
  // )
}

export const submitCollectionSearchNextPage = () => {
  // note that this function does *not* make any changes to the URL - including push the user to the collection view. it assumes that they are already there, and furthermore, that no changes to any filters that would update the URL have been made, since that implies a new search anyway

  return async (dispatch, getState) => {
    if (isRequestInvalid(getState())) {
      return // TODO test this!
    }
    dispatch(collectionMoreResultsRequested())
    const updatedFilterState = getFilterFromState(getState())

    const body = collectionBodyBuilder(updatedFilterState, false)
    if (!body) {
      dispatch(collectionSearchError('Invalid Request'))
      return
    }
    return fetchCollectionSearch(
      body,
      payload => {
        dispatch(collectionMoreResultsReceived(payload.data))
      },
      e => {
        // dispatch(showErrors(e.errors || e)) // TODO
        dispatch(collectionSearchError(e.errors || e))
      }
    )
  }

  // const prefetchHandler = dispatch => {
  //   dispatch(collectionMoreResultsRequested())
  // }
  //
  // const bodyBuilder = state => {
  //   return collectionBodyBuilder(getFilterFromState(state), false)
  // }
  //
  // const successHandler = (dispatch, payload) => {
  //   dispatch(collectionMoreResultsReceived(payload.data))
  // }
  //
  // return buildSearchAction(
  //   'collection',
  //   isRequestInvalid,
  //   prefetchHandler,
  //   bodyBuilder,
  //   successHandler,
  //   errorHandler
  // )
}

const navigateToCollectionUrl = (history, filterState) => {
  const locationDescriptor = encodeLocationDescriptor(
    ROUTE.collections,
    filterState
  )
  if (
    !_.isEmpty(locationDescriptor.search) &&
    isPathNew(history.location, locationDescriptor)
  ) {
    history.push(locationDescriptor)
  }
}
