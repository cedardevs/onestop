import _ from 'lodash'
import {buildSearchAction} from './AsyncHelpers'
import {showErrors} from '../ErrorActions'

import {assembleSearchRequest, encodeQueryString} from '../../utils/queryUtils'
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

const validRequestCheck = state => {
  const inFlight = state.search.collectionRequest.inFlight
  return !inFlight
}

const errorHandler = (dispatch, e) => {
  // dispatch(showErrors(e.errors || e)) // TODO show errors
  dispatch(collectionSearchError(e.errors || e))
}

const collectionFilterState = state => {
  return (state && state.search && state.search.collectionFilter) || {}
}

const collectionBodyBuilder = (filterState, requestFacets) => {
  const body = assembleSearchRequest(filterState, requestFacets)
  const hasQueries = body && body.queries && body.queries.length > 0
  const hasFilters = body && body.filters && body.filters.length > 0
  if (!(hasQueries || hasFilters)) {
    return undefined
  }
  return body
}

const newSearchSuccessHandler = (dispatch, payload) => {
  dispatch(
    collectionNewSearchResultsReceived(
      payload.meta.total,
      payload.data,
      payload.meta.facets
    )
  )
}

export const submitCollectionSearchWithQueryText = (history, queryText) => {
  const filterState = collectionFilter(
    initialState,
    collectionUpdateQueryText(queryText)
  )

  return submitCollectionSearchWithFilter(history, filterState)
}

export const submitCollectionSearchWithFilter = (history, filterState) => {
  // note: this updates the URL as well, it is not intended to be just a background search - make a new action if we need that case handled

  const prefetchHandler = dispatch => {
    dispatch(collectionNewSearchResetFiltersRequested(filterState))
    dispatch(updateURLAndNavigateToCollectionRoute(history, filterState))
  }

  const bodyBuilder = () => {
    return collectionBodyBuilder(filterState, true)
  }

  return buildSearchAction(
    'collection',
    validRequestCheck,
    prefetchHandler,
    bodyBuilder,
    newSearchSuccessHandler,
    errorHandler
  )
}

export const submitCollectionSearch = history => {
  // note: this updates the URL as well, it is not intended to be just a background search - make a new action if we need that case handled

  const prefetchHandler = (dispatch, state) => {
    const filterState = collectionFilterState(state)
    dispatch(collectionNewSearchRequested())
    dispatch(updateURLAndNavigateToCollectionRoute(history, filterState))
  }

  const bodyBuilder = state => {
    return collectionBodyBuilder(collectionFilterState(state), true)
  }

  return buildSearchAction(
    'collection',
    validRequestCheck,
    prefetchHandler,
    bodyBuilder,
    newSearchSuccessHandler,
    errorHandler
  )
}

export const submitCollectionSearchNextPage = () => {
  // note that this function does *not* make any changes to the URL - including push the user to the collection view. it assumes that they are already there, and furthermore, that no changes to any filters that would update the URL have been made, since that implies a new search anyway

  const prefetchHandler = dispatch => {
    dispatch(collectionMoreResultsRequested())
  }

  const bodyBuilder = state => {
    return collectionBodyBuilder(collectionFilterState(state), false)
  }

  const successHandler = (dispatch, payload) => {
    dispatch(collectionMoreResultsReceived(payload.data))
  }

  return buildSearchAction(
    'collection',
    validRequestCheck,
    prefetchHandler,
    bodyBuilder,
    successHandler,
    errorHandler
  )
}

const updateURLAndNavigateToCollectionRoute = (history, filterState) => {
  return dispatch => {
    const query = encodeQueryString(filterState)
    if (!_.isEmpty(query)) {
      const locationDescriptor = {
        pathname: '/collections',
        search: `?${query}`,
      }
      history.push(locationDescriptor)
    }
  }
}
