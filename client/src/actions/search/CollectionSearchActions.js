import _ from 'lodash'
import {buildSearchAction} from '../fetch/SearchActions'
import {showErrors} from '../ErrorActions'

import {assembleCollectionSearchRequest, encodeQueryString} from '../../utils/queryUtils'
import {
  collectionSearchStart,
  collectionSearchComplete,
  collectionSearchError,
} from './CollectionRequestActions'

export const asyncNewCollectionSearch = () => {
  return triggerCollectionSearch(true, true)
}
export const asyncMoreCollectionResults = () => {
  return triggerCollectionSearch(false, false)
}

const triggerCollectionSearch = (
  clearPreviousResults = false,
  retrieveFacets = true
) => {
  const validRequestCheck = state => {
    const inFlight =
      state.search.collectionRequest.collectionSearchRequestInFlight
    return !inFlight
  }
  const prefetchHandler = dispatch => {
    dispatch(collectionSearchStart(clearPreviousResults))
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
  const errorHandler = (dispatch, e) => {
    // dispatch(showErrors(e.errors || e)) // TODO show errors
    console.log(e.errors || e)
    dispatch(collectionSearchError(e.errors || e))
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

export const showCollections = history => {
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
