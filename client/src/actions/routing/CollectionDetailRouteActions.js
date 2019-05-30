import _ from 'lodash'
import {fetchGranuleSearch, fetchCollectionDetail} from './AsyncHelpers'
import {showErrors} from '../ErrorActions'

import {
  assembleSearchRequest,
  encodeLocationDescriptor,
  // decodeQueryString,
} from '../../utils/queryUtils'
import {ROUTE, isPathNew} from '../../utils/urlUtils'
import {
  collectionDetailRequested,
  collectionDetailReceived,
  collectionDetailError,
  granuleMatchingCountRequested,
  granuleMatchingCountReceived,
  granuleMatchingCountError,
} from './CollectionDetailStateActions'

const getFilterFromState = state => {
  return (state && state.search && state.search.collectionDetailFilter) || {}
}

const isRequestInvalid = (id, state) => {
  const {collectionDetailRequest} = state.search
  const inFlight =
    collectionDetailRequest.inFlight ||
    collectionDetailRequest.backgroundInFlight
  return inFlight || _.isEmpty(id)
}

const granuleBodyBuilder = filterState => {
  // TODO make the 0 page size a param of the other granuleBodyBuilder and reuse, so they won't be accidentally different?
  const body = assembleSearchRequest(filterState, false, 0)
  const hasQueries = body && body.queries && body.queries.length > 0
  const hasFilters = body && body.filters && body.filters.length > 0
  if (!(hasQueries || hasFilters)) {
    return undefined
  }
  return body
}

const helper = (dispatch, id, filterState) => {
  const body = granuleBodyBuilder(filterState)
  // if (!body) {
  //   dispatch(errorHandlerAction('Invalid Request')) // TODO this would be a horrible error, since it shouldn't be possible after the id check
  //   return
  // }

  // TODO I think this version avoids the parallel problem.. but not sure how to test
  const f1 = fetchCollectionDetail(
    id,
    payload => {
      dispatch(
        collectionDetailReceived(payload.data[0], payload.meta.totalGranules)
      )
    },
    e => {
      // dispatch(showErrors(e.errors || e)) // TODO
      dispatch(collectionDetailError(e.errors || e)) // TODO
    }
  )
  const f2 = fetchGranuleSearch(
    body,
    payload => {
      dispatch(granuleMatchingCountReceived(payload.meta.total))
    },
    e => {
      // dispatch(showErrors(e.errors || e)) // TODO
      dispatch(granuleMatchingCountError(e.errors || e)) // TODO
    }
  )

  return Promise.all([ f1, f2 ])
}

export const submitCollectionDetailAndUpdateUrl = (
  history,
  id,
  filterState
) => {
  return async (dispatch, getState) => {
    if (isRequestInvalid(id, getState())) {
      return
    }

    dispatch(collectionDetailRequested(id, filterState))
    dispatch(granuleMatchingCountRequested())
    const updatedFilterState = getFilterFromState(getState()) // use newly updated state from initializeRequestAction
    navigateToDetailUrl(history, updatedFilterState)
    return helper(dispatch, id, updatedFilterState)
    // try{ // TODO change these promises so that they can be fired off in parallel instead of succession
    //   let detail = await fetchCollectionDetail(id)
    //   checkForErrors(detail)
    //   console.log("?", detail.json())
    //   console.log("??", detail.body)
    //   dispatch(successHandlerAction(JSON.parse(detail.body)))
    // } catch(err) {
    //   console.log('???', err)
    //   dispatch(errorHandlerAction(err))
    //   // return
    // }
    // // let this one run so that (either way) backgroundInFlight gets correctly reset
    // try{let granule = await fetchGranuleSearch(body)
    //   checkForErrors(granule)
    //   dispatch(granuleSuccessHandlerAction(JSON.parse(granule.body)))
    // } catch(err) {
    //   console.log('????????', err)
    //   dispatch(granuleErrorHandlerAction(err))
    //   // return
    // }
    // dispatch(successHandlerAction(JSON.parse(detail.body), JSON.parse(granule.body)))
  }
}

export const submitCollectionDetail = (id, filterState) => {
  return async (dispatch, getState) => {
    if (isRequestInvalid(id, getState())) {
      return
    }

    dispatch(collectionDetailRequested(id, filterState))
    dispatch(granuleMatchingCountRequested())
    const updatedFilterState = getFilterFromState(getState()) // use newly updated state from initializeRequestAction

    return helper(dispatch, id, updatedFilterState)
  }
}

const navigateToDetailUrl = (history, filterState) => {
  const locationDescriptor = encodeLocationDescriptor(
    ROUTE.details,
    filterState
  )

  if (isPathNew(history.location, locationDescriptor)) {
    history.push(locationDescriptor)
  }
}
