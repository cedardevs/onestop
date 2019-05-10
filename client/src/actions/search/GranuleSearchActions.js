import _ from 'lodash'
import {buildSearchAction} from '../fetch/SearchActions'
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
} from './GranuleRequestActions'

/*
  Since granule results always use the same section of the redux store (because this is all tied to the same Route), a 'new' search and a 'more results' search use the same inFlight check so they can't clobber each other, among other things.
*/

export const asyncNewGranuleSearch = (history, id) => {
  // TODO rename to indicate that it updates the URL as well? - this is *not* just a background request - make a new action if we need that!!
  return buildNewGranuleSearch(history, id)
}
export const asyncMoreGranuleResults = () => {
  // note that this function does *not* make any changes to the URL - including push the user to the granule view. it assumes that they are already there, and furthermore, that no changes to any filters that would update the URL have been made, since that implies a new search anyway
  return buildMoreResultsSearch()
}

const validRequestCheck = state => {
  const inFlight = state.search.granuleRequest.granuleSearchRequestInFlight
  return !inFlight
}

const errorHandler = (dispatch, e) => {
  // dispatch(showErrors(e.errors || e)) // TODO show errors
  dispatch(granuleSearchError(e.errors || e))
}

const buildNewGranuleSearch = (history, id) => {
  // new granule search *for granules within a single collection*
  const prefetchHandler = dispatch => {
    dispatch(granuleNewSearchRequested(id))
    dispatch(updateURLAndNavigateToGranuleRoute(history, id))
  }

  const bodyBuilder = state => {
    const body = assembleGranuleSearchRequest(state, true) // TODO clean up these args...
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
        payload.meta
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

const buildMoreResultsSearch = () => {
  // fetch the next page of granules granule search *for granules within a single collection*
  const prefetchHandler = dispatch => {
    dispatch(granuleMoreResultsRequested())
  }

  const bodyBuilder = state => {
    const body = assembleGranuleSearchRequest(state, false) // TODO clean up these args...
    const hasQueries = body && body.queries && body.queries.length > 0
    const hasFilters = body && body.filters && body.filters.length > 0
    let selectedCollections = state.search.granuleFilter.selectedIds
    // TODO combine selectedCollections and id - in newSearch case, it knows the id from other contexts and/or should set it explicitly in the prefetch handler instead...
    if (!selectedCollections || !(hasQueries || hasFilters)) {
      // TODO returning an undefined body exits buildSearchAction early. It should resolve as an error or *something* - otherwise inFlight won't be reset! This is a consequence of moving the prefetch before this step. That also means that the bodyBuilder isn't really the right place to verify certain things are set...
      // Which leads to the question: under what conditions would it not have the correct queries and filters to make a sensible request? Only case I can think of is manually entering something weird in the URL.
      // but TLDR; instead of returning undefined throw an error?
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
    const query = encodeQueryString(getState(), 'granuleFilter') //TODO put the id in there too? - TODO and pull the getState() inputs back as parameters so 'granuleFilter' is not needed?
    const locationDescriptor = {
      pathname: `/collections/granules/${id}`, // TODO get this path from urlUtils.ROUTE?
      search: !_.isEmpty(query) ? `?${query}` : '',
    }
    history.push(locationDescriptor)
  }
}
