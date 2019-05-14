import {decodeQueryString} from '../utils/queryUtils'
import {
  getCollectionIdFromDetailPath,
  getCollectionIdFromGranuleListPath,
} from '../utils/urlUtils'
import {granuleUpdateFilters} from './routing/GranuleSearchStateActions'
import {
  asyncNewGranuleSearch,
  showGranules,
} from './routing/GranuleSearchRouteActions'
import {collectionUpdateFilters} from './routing/CollectionSearchStateActions'
import {getCollection} from './routing/CollectionDetailRouteActions' // TODO rename that action to async.... as wells
import {submitCollectionSearch} from './routing/CollectionSearchRouteActions'
import {fetchSitemap} from './fetch/FetchActions'
import {fetchConfig} from './ConfigActions'
import {fetchCounts, fetchInfo} from './fetch/InfoActions'

export const loadGranulesList = (history, path, newQueryString) => {
  return (dispatch, getState) => {
    if (newQueryString.indexOf('?') === 0) {
      newQueryString = newQueryString.slice(1)
    }
    const searchFromQuery = decodeQueryString(newQueryString)
    const searchFromState = _.get(getState(), 'search.granuleFilter')
    if (!_.isEqual(searchFromQuery, searchFromState)) {
      const detailId = getCollectionIdFromGranuleListPath(path)
      dispatch(granuleUpdateFilters(searchFromQuery))
      dispatch(asyncNewGranuleSearch(history, detailId)) // this updates the URL and push to that page, but in this context we are already there and no changes will be made by that particular step
    }
  }
}

export const loadCollections = (history, newQueryString) => {
  return (dispatch, getState) => {
    if (newQueryString.indexOf('?') === 0) {
      newQueryString = newQueryString.slice(1)
    }
    const searchFromQuery = decodeQueryString(newQueryString)
    const searchFromState = _.get(getState(), 'search.collectionFilter')
    if (!_.isEqual(searchFromQuery, searchFromState)) {
      dispatch(collectionUpdateFilters(searchFromQuery))
      dispatch(submitCollectionSearch(history)) // this updates the URL and push to that page, but in this context we are already there and no changes will be made by that particular step
    }
  }
}

export const loadDetails = path => {
  return (dispatch, getState) => {
    if (!getState().search.collectionDetailRequest.inFlight) {
      const detailId = getCollectionIdFromDetailPath(path)
      dispatch(getCollection(detailId))
    }
  }
}

export const initialize = () => {
  return dispatch => {
    dispatch(fetchConfig())
    dispatch(fetchInfo())
    dispatch(fetchCounts())
  }
}

export const getSitemap = () => {
  return fetchSitemap()
}
