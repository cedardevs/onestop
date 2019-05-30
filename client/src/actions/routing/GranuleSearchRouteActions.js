import _ from 'lodash'
import {fetchGranuleSearch} from './AsyncHelpers'
import {
  assembleSearchRequest,
  encodeLocationDescriptor,
} from '../../utils/queryUtils'
import {ROUTE, isPathNew} from '../../utils/urlUtils'
import {showErrors} from '../ErrorActions'
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

const granuleBodyBuilder = (filterState, requestFacets) => {
  // TODO did it work to pull collectionId check out?
  const body = assembleSearchRequest(filterState, requestFacets)
  const hasQueries = body && body.queries && body.queries.length > 0
  const hasFilters = body && body.filters && body.filters.length > 0
  if (!(hasQueries || hasFilters)) {
    return undefined
  }
  return body
}

export const submitGranuleSearchWithFilter = (
  history,
  collectionId,
  filterState
) => {
  return async (dispatch, getState) => {
    // note: this updates the URL as well, it is not intended to be just a background search - make a new action if we need that case handled

    if (isRequestInvalid(collectionId, getState())) {
      return // TODO test this!
    }

    dispatch(granuleNewSearchResetFiltersRequested(collectionId, filterState))
    const updatedFilterState = getFilterFromState(getState())
    navigateToGranuleUrl(history, collectionId, updatedFilterState)

    const body = granuleBodyBuilder(updatedFilterState, true)
    return fetchGranuleSearch(
      body,
      payload => {
        dispatch(
          granuleNewSearchResultsReceived(
            payload.meta.total,
            payload.data,
            payload.meta.facets
          )
        )
      },
      e => {
        // dispatch(showErrors(e.errors || e)) // TODO
        dispatch(granuleSearchError(e.errors || e))
      }
    )
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

    const body = granuleBodyBuilder(updatedFilterState, true)
    return fetchGranuleSearch(
      body,
      payload => {
        dispatch(
          granuleNewSearchResultsReceived(
            payload.meta.total,
            payload.data,
            payload.meta.facets
          )
        )
      },
      e => {
        // dispatch(showErrors(e.errors || e)) // TODO
        dispatch(granuleSearchError(e.errors || e))
      }
    )
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

    if (
      !updatedFilterState.selectedIds ||
      updatedFilterState.selectedIds.length == 0
    ) {
      return null
    } // TODO test me

    const body = granuleBodyBuilder(updatedFilterState, false)
    return fetchGranuleSearch(
      body,
      payload => {
        dispatch(granuleMoreResultsReceived(payload.data))
      },
      e => {
        // dispatch(showErrors(e.errors || e)) // TODO
        dispatch(granuleSearchError(e.errors || e))
      }
    )
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
