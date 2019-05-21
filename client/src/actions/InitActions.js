import {decodeQueryString} from '../utils/queryUtils'
import {
  getCollectionIdFromDetailPath,
  getCollectionIdFromGranuleListPath,
} from '../utils/urlUtils'
import {submitGranuleSearchWithFilter} from './routing/GranuleSearchRouteActions'
import {submitCollectionDetail} from './routing/CollectionDetailRouteActions'
import {submitCollectionSearchWithFilter} from './routing/CollectionSearchRouteActions'
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
      dispatch(
        submitGranuleSearchWithFilter(history, detailId, searchFromQuery)
      ) // this updates the URL and push to that page, but in this context we are already there and no changes will be made by that particular step
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
      dispatch(submitCollectionSearchWithFilter(history, searchFromQuery)) // this updates the URL and push to that page, but in this context we are already there and no changes will be made by that particular step
    }
  }
}

export const loadDetails = (history, path, newQueryString) => {
  return (dispatch, getState) => {
    const detailId = getCollectionIdFromDetailPath(path)

    const searchFromQuery = decodeQueryString(newQueryString)
    const searchFromState = _.get(getState(), 'search.collectionDetailFilter')
    // if (!_.isEqual(searchFromQuery, searchFromState)) { TODO put this check back in somewhere
    //   dispatch(granuleUpdateMatchingFilters(searchFromQuery))
    //   // TODO update collection query text to initial - to clear header search filter!
    // }

    dispatch(submitCollectionDetail(history, detailId, searchFromQuery))
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
