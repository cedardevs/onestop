import {decodePathAndQueryString} from '../utils/queryUtils'
import {submitGranuleSearchWithFilter} from './routing/GranuleSearchRouteActions'
import {
  submitCollectionDetail,
  submitCollectionDetailIsolated,
} from './routing/CollectionDetailRouteActions'
import {submitCollectionSearchWithFilter} from './routing/CollectionSearchRouteActions'
import {fetchSitemap} from './fetch/FetchActions'
import {fetchConfig} from './ConfigActions'
import {fetchCounts, fetchInfo} from './fetch/InfoActions'

export const loadGranulesList = (history, path, newQueryString) => {
  return (dispatch, getState) => {
    const {id, filters} = decodePathAndQueryString(path, newQueryString)
    if (areFiltersChanged(getState, 'search.granuleFilter', filters)) {
      dispatch(submitGranuleSearchWithFilter(history, id, filters))
    }
  }
}

export const loadCollections = (history, newQueryString) => {
  return (dispatch, getState) => {
    const {filters} = decodePathAndQueryString('', newQueryString)
    if (areFiltersChanged(getState, 'search.collectionFilter', filters)) {
      dispatch(submitCollectionSearchWithFilter(history, filters))
    }
  }
}

export const loadDetails = (history, path, newQueryString) => {
  return (dispatch, getState) => {
    const {id, filters} = decodePathAndQueryString(path, newQueryString)
    if (areFiltersChanged(getState, 'search.collectionDetailFilter', filters)) {
      dispatch(submitCollectionDetail(history, id, filters))
    }
  }
}

export const loadDetailsIsolated = collectionId => {
  return dispatch => {
    dispatch(submitCollectionDetailIsolated(collectionId))
  }
}

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
