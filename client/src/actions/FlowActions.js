import _ from 'lodash'
import watch from 'redux-watch'
import {push} from 'react-router-redux'
import {
  encodeQueryString,
  decodeQueryString,
  decodeLocation,
} from '../utils/queryUtils'
import {
  triggerSearch,
  fetchGranules,
  clearCollections,
  clearGranules,
  getCollection,
} from './SearchRequestActions'
import {
  updateSearch,
  clearSelections,
  toggleSelection,
} from './SearchParamActions'
import {fetchConfig} from './ConfigActions'
import {fetchInfo, fetchCounts} from './InfoActions'
import {hideLeft, showLeft} from './LayoutActions'
import store from '../store'

export const showCollections = (prefix = '') => {
  return (dispatch, getState) => {
    dispatch(clearSelections())
    const query = encodeQueryString(getState())
    if (!_.isEmpty(query)) {
      const locationDescriptor = {
        pathname: `${prefix}/collections`,
        search: `?${query}`,
      }
      dispatch(push(locationDescriptor))
    }
  }
}

export const showGranules = (prefix = '') => {
  // this is only needed for the 508 site now
  return (dispatch, getState) => {
    const query = encodeQueryString(getState())
    if (!_.isEmpty(query)) {
      const locationDescriptor = {
        pathname: `${prefix}/collections/files`,
        search: `?${query}`,
      }
      dispatch(push(locationDescriptor))
    }
  }
}

export const showDetails = id => {
  if (!id) {
    return
  }
  return (dispatch, getState) => {
    const query = encodeQueryString(getState())
    const locationDescriptor = {
      pathname: `collections/details/${id}`,
      search: _.isEmpty(query) ? null : `?${query}`,
    }
    dispatch(getCollection(id))
    dispatch(clearSelections())
    dispatch(toggleSelection(id))
    dispatch(push(locationDescriptor))
    dispatch(clearGranules())
    dispatch(fetchGranules())
  }
}

export const showHome = () => {
  return dispatch => {
    dispatch(updateSearch())
    dispatch(push(`/`))
    dispatch(clearCollections())
  }
}

export const TOGGLE_GRANULE_FOCUS = 'toggle_granule_focus'
export const toggleGranuleFocus = (id, bool) => {
  return {
    type: TOGGLE_GRANULE_FOCUS,
    id,
    focused: bool,
  }
}

export const LOADING_SHOW = 'LOADING_SHOW'
export const showLoading = () => {
  return {
    type: LOADING_SHOW,
  }
}

export const LOADING_HIDE = 'LOADING_HIDE'
export const hideLoading = () => {
  return {
    type: LOADING_HIDE,
  }
}

export const TOGGLE_BACKGROUND_IMAGE = 'TOGGLE_BACKGROUND_IMAGE'
const toggleBackgroundImage = boolVisible => {
  return {
    type: TOGGLE_BACKGROUND_IMAGE,
    visible: boolVisible,
  }
}

export const UPDATE_BOUNDS = 'UPDATE_BOUNDS'
export const updateBounds = (to, source) => {
  return {
    type: UPDATE_BOUNDS,
    to: to,
    source: source,
  }
}

export const initialize = () => {
  return dispatch => {
    dispatch(fetchConfig())
    dispatch(fetchInfo())
    dispatch(fetchCounts())
  }
}

const loadFromUrl = pathname => {
  // Note, collection queries are automatically updated by the URL because the query is parsed into search, which triggers loadData via a watch

  const detailIdRegex = /\/details\/([-\w]+)/
  const detailIdMatches = detailIdRegex.exec(pathname)

  const detailId =
    detailIdMatches && detailIdMatches[1] ? detailIdMatches[1] : null

  if (detailId) {
    store.dispatch(getCollection(detailId))
    store.dispatch(triggerSearch())
    store.dispatch(fetchGranules())
  }
}

export const loadData = () => {
  return (dispatch, getState) => {
    const state = getState()

    const collectionsSelected = !_.isEmpty(state.behavior.search.selectedIds)
    const granulesLoaded = !_.isEmpty(state.domain.results.granules)

    dispatch(triggerSearch())
    if (collectionsSelected && !granulesLoaded) {
      dispatch(fetchGranules())
    }
  }
}

// if the location query string changed and is out of sync with the query state,
// update the query state and load collection/granule data as needed
const applyNewQueryString = newQueryString => {
  if (newQueryString.indexOf('?') === 0) {
    newQueryString = newQueryString.slice(1)
  }
  const searchFromQuery = decodeQueryString(newQueryString)
  const searchFromState = _.get(store.getState(), 'behavior.search')
  if (!_.isEqual(searchFromQuery, searchFromState)) {
    store.dispatch(clearCollections())
    store.dispatch(clearGranules())
    store.dispatch(clearSelections())
    store.dispatch(updateSearch(searchFromQuery))
    store.dispatch(loadData())
  }
}

const queryWatch = watch(
  store.getState,
  'behavior.routing.locationBeforeTransitions.search'
)
store.subscribe(queryWatch(applyNewQueryString))

// Update background
const updateBackground = path => {
  const is508 =
    (_.startsWith(path, '/508/') && path !== '/508/') ||
    (_.startsWith(path, '508/') && path !== '508/')
  store.dispatch(toggleBackgroundImage(!is508)) //Cover strange routing case. TODO: Regex test?
}

const updateLayout = path => {
  const is508 =
    (_.startsWith(path, '/508/') && path !== '/508/') ||
    (_.startsWith(path, '508/') && path !== '508/')
  const detailIdRegex = /\/details\/([-\w]+)/
  const detailIdMatches = detailIdRegex.exec(path)
  if (is508 || detailIdMatches) {
    store.dispatch(hideLeft())
  }
  else {
    store.dispatch(showLeft())
  }
}

const pathWatch = watch(
  store.getState,
  'behavior.routing.locationBeforeTransitions.pathname'
)

const pathWatchUpdates = path => {
  updateBackground(path)
  updateLayout(path)
  loadFromUrl(path)
}
store.subscribe(pathWatch(pathWatchUpdates))
