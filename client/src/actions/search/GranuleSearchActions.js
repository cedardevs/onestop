import {buildSearchAction} from './SearchActions'
import {
  assembleGranuleSearchRequest,
  encodeQueryString,
} from '../../utils/queryUtils'
import {showErrors} from '../ErrorActions'
import {
  granuleSearchStart,
  granuleSearchComplete,
  granuleSearchError,
} from './GranuleRequestActions'

export const triggerGranuleSearch = (
  clearPreviousResults = false,
  retrieveFacets = true
) => {
  const bodyBuilder = state => {
    const body = assembleGranuleSearchRequest(state, false, retrieveFacets)
    const inFlight = state.search.granuleRequest.granuleSearchRequestInFlight
    const hasQueries = body && body.queries && body.queries.length > 0
    const hasFilters = body && body.filters && body.filters.length > 0
    let selectedCollections = state.search.collectionFilter.selectedIds
    if (inFlight || !selectedCollections || !(hasQueries || hasFilters)) {
      return undefined
    }
    return body
  }
  const prefetchHandler = dispatch => {
    dispatch(granuleSearchStart()) // TODO add params?
  }
  const successHandler = (dispatch, payload) => {
    dispatch(
      granuleSearchComplete(
        clearPreviousResults,
        payload.meta.total,
        payload.data,
        retrieveFacets ? payload.meta : null
      )
    )
  }
  const errorHandler = (dispatch, e) => {
    // dispatch(showErrors(e.errors || e)) // TODO show errors
    dispatch(granuleSearchError(e.errors || e))
  }

  return buildSearchAction(
    'granule',
    bodyBuilder,
    prefetchHandler,
    successHandler,
    errorHandler
  )
}

export const showGranules = (history, id) => {
  if (!id) {
    return
  }
  return (dispatch, getState) => {
    // dispatch(collectionClearSelectedIds()) TODO leaving this alone since we specifically do *not* want to reset the selected collection ID for granule searching, I think
    const query = encodeQueryString(getState(), 'granuleFilter') //TODO put the id in there too?
    const locationDescriptor = {
      pathname: `/collections/granules/${id}`, // TODO get this path from urlUtils.ROUTE?
      search: !_.isEmpty(query) ? `?${query}` : '',
    }
    history.push(locationDescriptor)
  }
}
