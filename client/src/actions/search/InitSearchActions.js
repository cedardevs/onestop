import {decodeQueryString} from '../../utils/queryUtils'
import {getCollectionIdFromGranuleListPath} from '../../utils/urlUtils'
import {granuleUpdateFilters} from './GranuleFilterActions'
import {triggerGranuleSearch} from './GranuleSearchActions'
import {
  collectionClearSelectedIds,
  collectionToggleSelectedId,
} from './CollectionFilterActions'
import {getCollection} from './CollectionSearchActions'
import {collectionClearDetailGranulesResult} from './CollectionResultActions'

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
