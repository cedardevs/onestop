import {decodeQueryString} from '../../utils/queryUtils'
import {
  getCollectionIdFromDetailPath,
  getCollectionIdFromGranuleListPath,
} from '../../utils/urlUtils'
import {granuleUpdateFilters} from './GranuleFilterActions'
import {triggerGranuleSearch} from './GranuleSearchActions'
import {
  collectionClearSelectedIds,
  collectionToggleSelectedId,
  collectionUpdateFilters,
} from './CollectionFilterActions'
import {getCollection} from '../get/CollectionGetDetailActions'
import {triggerCollectionSearch} from './CollectionSearchActions'
import {
  collectionClearResults,
  collectionClearDetailGranulesResult, // TODO make sure this still works!
} from './CollectionResultActions'
import {buildSitemapAction} from './SearchActions'
import {fetchConfig} from '../ConfigActions'
import {fetchCounts, fetchInfo} from './InfoActions'

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
      dispatch(collectionClearSelectedIds())
      dispatch(collectionToggleSelectedId(detailId))
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
      dispatch(collectionClearSelectedIds())
      dispatch(collectionUpdateFilters(searchFromQuery))
      dispatch(triggerCollectionSearch())
    }
  }
}

export const loadDetails = path => {
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
