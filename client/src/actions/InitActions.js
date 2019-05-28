import {decodePathAndQueryString} from '../utils/queryUtils'
// import {
//   // getCollectionIdFromDetailPath,
//   getCollectionIdFromGranuleListPath,
//   ROUTE
// } from '../utils/urlUtils'
import {submitGranuleSearchWithFilter} from './routing/GranuleSearchRouteActions'
import {submitCollectionDetail} from './routing/CollectionDetailRouteActions'
import {submitCollectionSearchWithFilter} from './routing/CollectionSearchRouteActions'
import {fetchSitemap} from './fetch/FetchActions'
import {fetchConfig} from './ConfigActions'
import {fetchCounts, fetchInfo} from './fetch/InfoActions'

export const loadGranulesList = (history, path, newQueryString) => {
  return (dispatch, getState) => {
    // if (newQueryString.indexOf('?') === 0) {
    //   newQueryString = newQueryString.slice(1)
    // }
    // const searchFromQuery = decodeQueryString(newQueryString)
    // const searchFromState = _.get(getState(), 'search.granuleFilter')
    // if (!_.isEqual(searchFromQuery, searchFromState)) {
    //   const detailId = getCollectionIdFromGranuleListPath(path)
    //   dispatch(
    //     submitGranuleSearchWithFilter(history, detailId, searchFromQuery)
    //   ) // this updates the URL and push to that page, but in this context we are already there and no changes will be made by that particular step
    // }
    const {id, filters} = decodePathAndQueryString(path, newQueryString)
    if (areFiltersChanged(getState, 'search.granuleFilter', filters)) {
      dispatch(submitGranuleSearchWithFilter(history, id, filters))
    }
  }
}

export const loadCollections = (history, newQueryString) => {
  return (dispatch, getState) => {
    // if (newQueryString.indexOf('?') === 0) {
    //   newQueryString = newQueryString.slice(1)
    // }
    // const searchFromQuery = decodeQueryString(newQueryString)
    // const searchFromState = _.get(getState(), 'search.collectionFilter')
    // if (!_.isEqual(searchFromQuery, searchFromState)) {
    //   dispatch(submitCollectionSearchWithFilter(history, searchFromQuery)) // this updates the URL and push to that page, but in this context we are already there and no changes will be made by that particular step
    // }
    const {filters} = decodePathAndQueryString('', newQueryString)
    if (areFiltersChanged(getState, 'search.collectionFilter', filters)) {
      dispatch(submitCollectionSearchWithFilter(history, filters))
    }
  }
}

export const loadDetails = (history, path, newQueryString) => {
  return (dispatch, getState) => {
    // const detailId = getCollectionIdFromDetailPath(path)
    //
    // if (newQueryString.indexOf('?') === 0) {
    //   newQueryString = newQueryString.slice(1)
    // }
    // const searchFromQuery = decodeQueryString(newQueryString)
    //
    // const searchFromState = _.get(getState(), 'search.collectionDetailFilter')
    // // if (!_.isEqual(searchFromQuery, searchFromState)) { TODO put this check back in somewhere
    // //   dispatch(granuleUpdateMatchingFilters(searchFromQuery))
    // //   // TODO update collection query text to initial - to clear header search filter!
    // // }
    // if (!_.isEqual(searchFromQuery, searchFromState)) {
    //
    //
    //   dispatch(submitCollectionDetail(history, detailId, searchFromQuery))
    // }
    const {id, filters} = decodePathAndQueryString(path, newQueryString)
    if (areFiltersChanged(getState, 'search.collectionDetailFilter', filters)) {
      dispatch(submitCollectionDetail(history, id, filters))
    }
  }
}
/* TODO change the above method to:
=> {
  const {id, filters} = decodePathAndQueryString(route.detail, path, newQueryString)
  if(areFiltersChanged(getState, 'search.collectionDetailFilter', filters)) {
        dispatch(submitCollectionDetail(history, id, filters))
  }
}
*/

const areFiltersChanged = (getState, filterName, newFilters) => {
  const stateFilters = _.get(getState(), filterName)
  return !_.isEqual(newFilters, stateFilters)
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
