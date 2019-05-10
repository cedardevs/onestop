import _ from 'lodash'
import {buildSearchAction} from '../fetch/SearchActions'
import {showErrors} from '../ErrorActions'

import {
  assembleCollectionSearchRequest,
  encodeQueryString,
} from '../../utils/queryUtils'
import {
  collectionNewSearchRequested,
  collectionMoreResultsRequested,
  collectionSearchComplete,
  collectionSearchError,
} from './CollectionRequestActions'

/*
  Since collection results always use the same section of the redux store (because this is all tied to the same Route), a 'new' search and a 'more results' search use the same inFlight check so they can't clobber each other, among other things.
*/
export const asyncNewCollectionSearch = history => {
  // TODO rename to indicate that it updates the URL as well? - this is *not* just a background request - make a new action if we need that!!
  return buildNewCollectionSearch(history)
}

export const asyncMoreCollectionResults = () => {
  // note that this function does *not* make any changes to the URL - including push the user to the collection view. it assumes that they are already there, and furthermore, that no changes to any filters that would update the URL have been made, since that implies a new search anyway
  return buildMoreResultsSearch()
}

const validRequestCheck = state => {
  const inFlight =
    state.search.collectionRequest.collectionSearchRequestInFlight
  return !inFlight
}

const errorHandler = (dispatch, e) => {
  // dispatch(showErrors(e.errors || e)) // TODO show errors
  console.log(e.errors || e)
  dispatch(collectionSearchError(e.errors || e))
}

const buildNewCollectionSearch = history => {
  // new collection search
  const prefetchHandler = dispatch => {
    dispatch(collectionNewSearchRequested())
    dispatch(updateURLAndNavigateToCollectionRoute(history))
  }

  const bodyBuilder = state => {
    const body = assembleCollectionSearchRequest(state, true)
    const hasQueries = body && body.queries && body.queries.length > 0
    const hasFilters = body && body.filters && body.filters.length > 0
    if (!(hasQueries || hasFilters)) {
      return undefined
    }
    return body
  }

  const successHandler = (dispatch, payload) => {
    const result = _.reduce(
      // TODO is this the right place to do the reduce? or in the ... reducer...
      payload.data,
      (map, resource) => {
        return map.set(
          resource.id,
          _.assign({type: resource.type}, resource.attributes)
        )
      },
      new Map()
    )

    dispatch(
      collectionSearchComplete(true, payload.meta.total, result, payload.meta)
    )
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

const triggerCollectionSearch = (
  // TODO change to buildMoreResultsSearch
  clearPreviousResults = false,
  retrieveFacets = true
) => {
  const prefetchHandler = dispatch => {
    dispatch(collectionMoreResultsRequested())
  }
  const bodyBuilder = state => {
    const body = assembleCollectionSearchRequest(state, retrieveFacets)
    const hasQueries = body && body.queries && body.queries.length > 0
    const hasFilters = body && body.filters && body.filters.length > 0
    if (!(hasQueries || hasFilters)) {
      return undefined
    }
    return body
  }
  const successHandler = (dispatch, payload) => {
    const result = _.reduce(
      // TODO is this the right place to do the reduce? or in the ... reducer...
      payload.data,
      (map, resource) => {
        return map.set(
          resource.id,
          _.assign({type: resource.type}, resource.attributes)
        )
      },
      new Map()
    )

    dispatch(
      collectionSearchComplete(
        clearPreviousResults,
        payload.meta.total,
        result,
        retrieveFacets ? payload.meta : null
      )
    )
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

const updateURLAndNavigateToCollectionRoute = history => {
  // was showCollections
  return (dispatch, getState) => {
    const query = encodeQueryString(getState())
    if (!_.isEmpty(query)) {
      const locationDescriptor = {
        pathname: '/collections',
        search: `?${query}`,
      }
      history.push(locationDescriptor)
    }
  }
}
