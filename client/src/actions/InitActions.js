import {decodeQueryString} from '../utils/queryUtils'
import {
  getCollectionIdFromDetailPath,
  getCollectionIdFromGranuleListPath,
} from '../utils/urlUtils'
import {granuleUpdateFilters} from './search/GranuleFilterActions'
import {
  asyncNewGranuleSearch,
  showGranules,
} from './search/GranuleSearchActions'
import {collectionUpdateFilters} from './search/CollectionFilterActions'
import {getCollection} from './get/CollectionGetDetailActions' // TODO rename that action to async.... as wells
import {asyncNewCollectionSearch} from './search/CollectionSearchActions'
// import {
//   collectionClearDetailGranulesResult, // TODO make sure this still works!
// } from './search/CollectionResultActions'
import {buildSitemapAction} from './fetch/SearchActions'
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
      dispatch(getCollection(detailId)) // TODO is this still really needed? And if so, why?
      // dispatch(collectionClearDetailGranulesResult())
      dispatch(granuleUpdateFilters(searchFromQuery))
      dispatch(asyncNewGranuleSearch(history, detailId))
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
      // dispatch(collectionClearDetailGranulesResult())
      // dispatch(collectionClearSelectedIds()) // TODO this implies that selectedIds is being overloaded in some way, or is too tied to a particular workflow...
      dispatch(collectionUpdateFilters(searchFromQuery))
      dispatch(asyncNewCollectionSearch(history))
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
