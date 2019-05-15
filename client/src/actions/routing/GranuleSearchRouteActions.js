import _ from 'lodash'
import {buildSearchAction} from './AsyncHelpers'
import {
  assembleGranuleSearchRequest,
  encodeQueryString,
} from '../../utils/queryUtils'
import {showErrors} from '../ErrorActions'
import {
  granuleNewSearchRequested,
  granuleMoreResultsRequested,
  granuleNewSearchResultsRecieved,
  granuleMoreResultsRecieved,
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

export const submitGranuleSearch = (history, id) => {
  // new granule search *for granules within a single collection*
  // note: this updates the URL as well, it is not intended to be just a background search - make a new action if we need that case handled

  const prefetchHandler = dispatch => {
    dispatch(granuleNewSearchRequested(id))
    dispatch(updateURLAndNavigateToGranuleRoute(history, id))
  }

  const bodyBuilder = state => {
    const body = assembleGranuleSearchRequest(state, true)
    const hasQueries = body && body.queries && body.queries.length > 0
    const hasFilters = body && body.filters && body.filters.length > 0
    let selectedCollections = state.search.granuleFilter.selectedIds
    // TODO combine selectedCollections and id - in newSearch case, it knows the id from other contexts and/or should set it explicitly in the prefetch handler instead...
    if (!selectedCollections || !(hasQueries || hasFilters)) {
      return undefined
    }
    return body
  }

  const successHandler = (dispatch, payload) => {
    dispatch(
      granuleNewSearchResultsRecieved(
        payload.meta.total,
        payload.data,
        payload.meta.facets
      )
    )
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

export const submitGranuleSearchNextPage = () => {
  // note that this function does *not* make any changes to the URL - including push the user to the granule view. it assumes that they are already there, and furthermore, that no changes to any filters that would update the URL have been made, since that implies a new search anyway
  // fetch the next page of granules granule search *for granules within a single collection*
  const prefetchHandler = dispatch => {
    dispatch(granuleMoreResultsRequested())
  }

  const bodyBuilder = state => {
    const body = assembleGranuleSearchRequest(state, false)
    const hasQueries = body && body.queries && body.queries.length > 0
    const hasFilters = body && body.filters && body.filters.length > 0
    let selectedCollections = state.search.granuleFilter.selectedIds
    // TODO combine selectedCollections and id - in newSearch case, it knows the id from other contexts and/or should set it explicitly in the prefetch handler instead...
    if (!selectedCollections || !(hasQueries || hasFilters)) {
      return undefined
    }
    return body
  }

  const successHandler = (dispatch, payload) => {
    dispatch(granuleMoreResultsRecieved(payload.data))
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

const updateURLAndNavigateToGranuleRoute = (history, id) => {
  // formerly showGranules - TODO rename the other comparable functions
  if (!id) {
    return
  }
  return (dispatch, getState) => {
    const state = getState()
    const query = encodeQueryString(
      (state && state.search && state.search.granuleFilter) || {}
    ) //TODO put the id in there too?
    const locationDescriptor = {
      pathname: `/collections/granules/${id}`, // TODO get this path from urlUtils.ROUTE?
      search: !_.isEmpty(query) ? `?${query}` : '',
    }
    history.push(locationDescriptor)
  }
}
