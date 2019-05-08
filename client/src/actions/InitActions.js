import {decodeQueryString} from '../utils/queryUtils'
import {
  getCollectionIdFromDetailPath,
  getCollectionIdFromGranuleListPath,
} from '../utils/urlUtils'
import {granuleUpdateFilters} from './search/GranuleFilterActions'
import {triggerGranuleSearch} from './search/GranuleSearchActions'
import {
  collectionUpdateFilters,
} from './search/CollectionFilterActions'
import {getCollection} from './get/CollectionGetDetailActions'
import {triggerCollectionSearch} from './search/CollectionSearchActions'
import {
  collectionClearResults,
  collectionClearDetailGranulesResult, // TODO make sure this still works!
} from './search/CollectionResultActions'
import {buildSitemapAction} from './search/SearchActions'
import {fetchConfig} from './ConfigActions'
import {fetchCounts, fetchInfo} from './search/InfoActions'

export const loadGranulesList = (path, newQueryString) => {
  return (dispatch, getState) => {
    if (newQueryString.indexOf('?') === 0) {
      newQueryString = newQueryString.slice(1)
    }
    const searchFromQuery = decodeQueryString(newQueryString)
    const searchFromState = _.get(getState(), 'search.granuleFilter')
    if (!_.isEqual(searchFromQuery, searchFromState)) {
      const detailId = getCollectionIdFromGranuleListPath(path)
      dispatch(getCollection(detailId))
      dispatch(collectionClearDetailGranulesResult())
      dispatch(granuleUpdateFilters(searchFromQuery))
      dispatch(triggerGranuleSearch())
    }
  }
}

export const loadCollections = newQueryString => {
  return (dispatch, getState) => {
    if (newQueryString.indexOf('?') === 0) {
      newQueryString = newQueryString.slice(1)
    }
    const searchFromQuery = decodeQueryString(newQueryString)
    const searchFromState = _.get(getState(), 'search.collectionFilter')
    if (!_.isEqual(searchFromQuery, searchFromState)) {
      dispatch(collectionClearResults())
      dispatch(collectionClearDetailGranulesResult())
      // dispatch(collectionClearSelectedIds()) // TODO this implies that selectedIds is being overloaded in some way, or is too tied to a particular workflow...
      dispatch(collectionUpdateFilters(searchFromQuery))
      dispatch(triggerCollectionSearch())
    }
  }
}

export const loadDetails = path => {
  console.log('it should load details now...', path)
  return (dispatch, getState) => {
    if (
      !getState().search.collectionDetailRequest.collectionDetailRequestInFlight
    ) {
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
  return buildSitemapAction()
}
