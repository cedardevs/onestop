import _ from 'lodash'
import {fetchGranuleSearch} from './AsyncHelpers'
import {
  assembleSearchRequest,
  encodeLocationDescriptor,
} from '../../utils/queryUtils'
import {ROUTE, isPathNew} from '../../utils/urlUtils'
import {
  granuleNewSearchRequested,
  granuleNewSearchResetFiltersRequested,
  granuleMoreResultsRequested,
  granuleNewSearchResultsReceived,
  granuleMoreResultsReceived,
  granuleSearchError,
} from './GranuleSearchStateActions'

/*
  Since granule results always use the same section of the redux store (because this is all tied to the same Route), a 'new' search and a 'more results' search use the same inFlight check so they can't clobber each other, among other things.
*/

const getFilterFromState = state => {
  return (state && state.search && state.search.granuleFilter) || {}
}

const isAlreadyInFlight = state => {
  const inFlight = state.search.granuleRequest.inFlight
  return inFlight
}

const isRequestInvalid = (id, state) => {
  return isAlreadyInFlight(state) || _.isEmpty(id)
}

export const granuleBodyBuilder = (filterState, requestFacets, pageSize) => {
  const body = assembleSearchRequest(filterState, requestFacets, pageSize)
  const hasQueries = body && body.queries && body.queries.length > 0
  const hasFilters = body && body.filters && body.filters.length > 0
  if (!(hasQueries || hasFilters)) {
    return undefined
  }
  return body
}

const newSearchSuccessHandler = dispatch => {
  return payload => {
    dispatch(
      granuleNewSearchResultsReceived(
        payload.meta.total,
        payload.data,
        payload.meta.facets
      )
    )
  }
}

const pageSuccessHandler = dispatch => {
  return payload => {
    dispatch(granuleMoreResultsReceived(payload.data))
  }
}

const helper = (dispatch, filterState, requestFacets, successHandler) => {
  // TODO rename this
  if (!filterState.selectedIds || filterState.selectedIds.length == 0) {
    dispatch(granuleSearchError('Invalid Request'))
    return
  }

  const body = granuleBodyBuilder(filterState, requestFacets)
  if (!body) {
    // not covered by tests, since this should never actually occur due to the collectionId being provided, but included to prevent accidentally sending off really unreasonable requests
    dispatch(granuleSearchError('Invalid Request'))
    return
  }
  return fetchGranuleSearch(body, successHandler(dispatch), e => {
    dispatch(granuleSearchError(e.errors || e))
  })
}

export const submitGranuleSearchWithFilter = (
  history,
  collectionId,
  filterState
) => {
  return async (dispatch, getState) => {
    // note: this updates the URL as well, it is not intended to be just a background search - make a new action if we need that case handled

    if (isRequestInvalid(collectionId, getState())) {
      return
    }

    dispatch(granuleNewSearchResetFiltersRequested(collectionId, filterState))
    const updatedFilterState = getFilterFromState(getState())
    navigateToGranuleUrl(history, collectionId, updatedFilterState)

    return helper(dispatch, updatedFilterState, true, newSearchSuccessHandler)
  }
}

export const submitGranuleSearch = (history, collectionId) => {
  // new granule search *for granules within a single collection*
  // note: this updates the URL as well, it is not intended to be just a background search - make a new action if we need that case handled

  return async (dispatch, getState) => {
    if (isRequestInvalid(collectionId, getState())) {
      return
    }

    dispatch(granuleNewSearchRequested(collectionId))
    const updatedFilterState = getFilterFromState(getState())
    navigateToGranuleUrl(history, collectionId, updatedFilterState)

    return helper(dispatch, updatedFilterState, true, newSearchSuccessHandler)
  }
}

export const submitGranuleSearchNextPage = () => {
  // note that this function does *not* make any changes to the URL - including push the user to the granule view. it assumes that they are already there, and furthermore, that no changes to any filters that would update the URL have been made, since that implies a new search anyway
  // fetch the next page of granules granule search *for granules within a single collection*
  return async (dispatch, getState) => {
    if (isAlreadyInFlight(getState())) {
      return
    }

    dispatch(granuleMoreResultsRequested())
    const updatedFilterState = getFilterFromState(getState())
    return helper(dispatch, updatedFilterState, false, pageSuccessHandler)
  }
}

const navigateToGranuleUrl = (history, collectionId, filterState) => {
  if (!collectionId) {
    return
  }
  const locationDescriptor = encodeLocationDescriptor(
    ROUTE.granules,
    filterState,
    collectionId
  )
  if (isPathNew(history.location, locationDescriptor)) {
    history.push(locationDescriptor)
  }
}
