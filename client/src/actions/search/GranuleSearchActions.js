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
  Since granule results always use the same section of the redux store, a 'new' search and a 'more results' search use the same inFlight check so they can't clobber each other.
*/

export const asyncNewGranuleSearch = (history, id) => { // TODO rename to indicate that it updates the URL as well? - this is *not* just a background request - make a new action if we need that!!
  return buildNewGranuleSearch(history, id)
}
export const asyncMoreGranuleResults = () => {
  return triggerGranuleSearch(null, null, false, false) // TODO
}

const validRequestCheck = state => {
  const inFlight = state.search.granuleRequest.granuleSearchRequestInFlight
  return !inFlight
}

// const bodyBuilder = state => {
//   const body = assembleGranuleSearchRequest(state, false, retrieveFacets)
//   const hasQueries = body && body.queries && body.queries.length > 0
//   const hasFilters = body && body.filters && body.filters.length > 0
//   let selectedCollections = state.search.collectionFilter.selectedIds
//   // TODO combine selectedCollections and id - in newSearch case, it knows the id from other contexts and/or should set it explicitly in the prefetch handler instead...
//   if (!selectedCollections || !(hasQueries || hasFilters)) {
//     return undefined
//   }
//   return body
// }


const errorHandler = (dispatch, e) => {
  // dispatch(showErrors(e.errors || e)) // TODO show errors
  dispatch(granuleSearchError(e.errors || e))
}

const buildNewGranuleSearch = (history, id) => {
  // new granule search *for granules within a single collection*
  const prefetchHandler = dispatch => {
    dispatch(granuleNewSearchRequested()) // TODO add the id as a param and set the selected id?
    dispatch(updateURLAndNavigateToGranuleRoute(history, id))
  }

  const bodyBuilder = state => {
    const body = assembleGranuleSearchRequest(state, false, true) // TODO clean up these args...
    const hasQueries = body && body.queries && body.queries.length > 0
    const hasFilters = body && body.filters && body.filters.length > 0
    let selectedCollections = state.search.collectionFilter.selectedIds
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


const triggerGranuleSearch = ( // trigger granule search *for granules within a single collection*
  history, id,
  clearPreviousResults = false, // TODO done with this arg now!
  retrieveFacets = true
) => {
  const prefetchHandler = dispatch => {
    dispatch(granuleMoreResultsRequested())
  }
  const bodyBuilder = state => {
    const body = assembleGranuleSearchRequest(state, false, false) // TODO clean up these args...
    const hasQueries = body && body.queries && body.queries.length > 0
    const hasFilters = body && body.filters && body.filters.length > 0
    let selectedCollections = state.search.collectionFilter.selectedIds
    // TODO combine selectedCollections and id - in newSearch case, it knows the id from other contexts and/or should set it explicitly in the prefetch handler instead...
    if (!selectedCollections || !(hasQueries || hasFilters)) {
      return undefined
    }
    return body
  }
  const successHandler = (dispatch, payload) => {
    dispatch(
      granuleMoreResultsRecieved(
        payload.meta.total,
        payload.data,
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

const updateURLAndNavigateToGranuleRoute = (history, id) => { // formerly showGranules - TODO rename the other comparable functions
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
