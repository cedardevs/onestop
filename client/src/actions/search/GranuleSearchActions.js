import {buildSearchAction} from '../fetch/SearchActions'
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

export const asyncNewGranuleSearch = () => {
  return triggerGranuleSearch(true, true)
}
export const asyncMoreGranuleResults = () => {
  return triggerGranuleSearch(false, false)
}

const triggerGranuleSearch = (
  clearPreviousResults = false,
  retrieveFacets = true
) => {
  const validRequestCheck = state => {
    const inFlight = state.search.granuleRequest.granuleSearchRequestInFlight
    return !inFlight
  }
  const prefetchHandler = dispatch => {
    dispatch(granuleSearchStart(clearPreviousResults))
  }

  const bodyBuilder = state => {
    const body = assembleGranuleSearchRequest(state, false, retrieveFacets)
    const hasQueries = body && body.queries && body.queries.length > 0
    const hasFilters = body && body.filters && body.filters.length > 0
    let selectedCollections = state.search.collectionFilter.selectedIds
    if (!selectedCollections || !(hasQueries || hasFilters)) {
      return undefined
    }
    return body
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
    validRequestCheck,
    prefetchHandler,
    bodyBuilder,
    successHandler,
    errorHandler
  )
}

export const showGranules = (history, id) => {
  // TODO rename these functions to update URL ?
  if (!id) {
    return
  }
  return (dispatch, getState) => {
    const query = encodeQueryString(getState(), 'granuleFilter') //TODO put the id in there too?
    const locationDescriptor = {
      pathname: `/collections/granules/${id}`, // TODO get this path from urlUtils.ROUTE?
      search: !_.isEmpty(query) ? `?${query}` : '',
    }
    history.push(locationDescriptor)
  }
}
