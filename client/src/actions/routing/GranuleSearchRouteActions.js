import _ from 'lodash'
import {buildSearchAction} from './AsyncHelpers'
import {
  assembleSearchRequest,
  encodePathAndQueryString,
  // decodeQueryString,
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

const validRequestCheck = state => {
  const inFlight = state.search.granuleRequest.inFlight
  return !inFlight
}

const errorHandler = (dispatch, e) => {
  // dispatch(showErrors(e.errors || e)) // TODO show errors
  dispatch(granuleSearchError(e.errors || e))
}

const granuleFilterState = state => {
  return (state && state.search && state.search.granuleFilter) || {}
}

const newSearchSuccessHandler = (dispatch, payload) => {
  dispatch(
    granuleNewSearchResultsReceived(
      payload.meta.total,
      payload.data,
      payload.meta.facets
    )
  )
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
  // note: this updates the URL as well, it is not intended to be just a background search - make a new action if we need that case handled

  if (!collectionId) {
    return
  } // TODO test this!

  const prefetchHandler = dispatch => {
    dispatch(granuleNewSearchResetFiltersRequested(collectionId, filterState))
    dispatch(
      updateURLAndNavigateToGranuleRoute(history, collectionId, filterState)
    )
  }

  const bodyBuilder = () => {
    return granuleBodyBuilder(filterState, true)
  }

  return buildSearchAction(
    'granule',
    validRequestCheck,
    prefetchHandler,
    bodyBuilder,
    newSearchSuccessHandler,
    errorHandler
  )
}

export const submitGranuleSearch = (history, collectionId) => {
  // new granule search *for granules within a single collection*
  // note: this updates the URL as well, it is not intended to be just a background search - make a new action if we need that case handled

  if (!collectionId) {
    return
  } // TODO test this!

  const prefetchHandler = (dispatch, state) => {
    const filterState = granuleFilterState(state)
    dispatch(granuleNewSearchRequested(collectionId))
    dispatch(
      updateURLAndNavigateToGranuleRoute(history, collectionId, filterState)
    )
  }

  const bodyBuilder = state => {
    const filterState = granuleFilterState(state)
    return granuleBodyBuilder(filterState, true)
  }

  return buildSearchAction(
    'granule',
    validRequestCheck,
    prefetchHandler,
    bodyBuilder,
    newSearchSuccessHandler,
    errorHandler
  )
}

export const submitGranuleSearchNextPage = () => {
  // note that this function does *not* make any changes to the URL - including push the user to the granule view. it assumes that they are already there, and furthermore, that no changes to any filters that would update the URL have been made, since that implies a new search anyway
  // fetch the next page of granules granule search *for granules within a single collection*
  const prefetchHandler = dispatch => {
    dispatch(granuleMoreResultsRequested())
  }

  const bodyBuilder = state => {
    const filterState = granuleFilterState(state)
    if (!filterState.selectedIds || filterState.selectedIds.length == 0) {
      return null
    } // TODO test me

    return granuleBodyBuilder(filterState, false)
  }

  const successHandler = (dispatch, payload) => {
    dispatch(granuleMoreResultsReceived(payload.data))
  }

  return buildSearchAction(
    'granule',
    validRequestCheck,
    prefetchHandler,
    bodyBuilder,
    successHandler,
    errorHandler
  )
}

const updateURLAndNavigateToGranuleRoute = (
  history,
  collectionId,
  filterState
) => {
  if (!collectionId) {
    return
  }
  return dispatch => {
    const locationDescriptor = encodePathAndQueryString(
      ROUTE.granules,
      filterState,
      collectionId
    )
    if (isPathNew(history.location, locationDescriptor)) {
      history.push(locationDescriptor)
    }

    // const query = encodeQueryString(filterState)
    // let currentURLQueryString = history.location.search
    // if (currentURLQueryString.indexOf('?') === 0) {
    //   currentURLQueryString = currentURLQueryString.slice(1)
    // }
    // const currentURLQuery = decodeQueryString(currentURLQueryString)
    // console.log('granule url', query, filterState)
    // if (
    //   !(
    //     history.location.path == `/collections/granules/${collectionId}` &&
    //     currentURLQueryString == query
    //   )
    // ) {
    //   const locationDescriptor = {
    //     pathname: `/collections/granules/${collectionId}`, // TODO get this path from urlUtils.ROUTE?
    //     search: !_.isEmpty(query) ? `?${query}` : '',
    //   }
    //   history.push(locationDescriptor)
    // }
  }
}
