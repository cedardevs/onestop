import _ from 'lodash'
import watch from 'redux-watch'
import { push } from 'react-router-redux'
import { encodeQueryString, decodeQueryString } from '../utils/queryUtils'
import { triggerSearch, fetchGranules, clearCollections, clearGranules } from './SearchRequestActions'
import { updateSearch } from './SearchParamActions'
import { fetchConfig } from './ConfigActions'
import {fetchInfo, fetchCounts} from './InfoActions'
import store from '../store'

export const showCollections = (prefix = '') => {
  return (dispatch, getState) => {
    const query = encodeQueryString(getState())
    if (!_.isEmpty(query)) {
      const locationDescriptor = {
        pathname: `${prefix}/collections`,
        search: `?${query}`
      }
      dispatch(push(locationDescriptor))
    }
  }
}

export const showGranules = (prefix = '') => {
  return (dispatch, getState) => {
    const query = encodeQueryString(getState())
    if (!_.isEmpty(query)) {
      const locationDescriptor = {
        pathname: `${prefix}/collections/files`,
        search: `?${query}`
      }
      dispatch(push(locationDescriptor))
    }
  }
}

export const showHome = () => {
  return (dispatch) => {
    dispatch(updateSearch())
    dispatch(push(`/`))
    dispatch(clearCollections())
  }
}

export const TOGGLE_HELP = 'TOGGLE_HELP'
export const toggleHelp = () => {
  return {
    type: TOGGLE_HELP
  }
}

export const TOGGLE_ABOUT = 'TOGGLE_ABOUT'
export const toggleAbout = () => {
  return {
    type: TOGGLE_ABOUT
  }
}

export const SET_FOCUS = 'SET_FOCUS'
export const setFocus = (id) => {
  return {
    type: SET_FOCUS,
    id: id
  }
}

export const TOGGLE_GRANULE_FOCUS = 'toggle_granule_focus'
export const toggleGranuleFocus = (id) => {
  return {
    type: TOGGLE_GRANULE_FOCUS,
    id
  }
}

export const LOADING_SHOW = 'LOADING_SHOW'
export const showLoading = () => {
  return {
    type: LOADING_SHOW
  }
}

export const LOADING_HIDE = 'LOADING_HIDE'
export const hideLoading = () => {
  return {
    type: LOADING_HIDE
  }
}

export const TOGGLE_BACKGROUND_IMAGE = 'TOGGLE_BACKGROUND_IMAGE'
const toggleBackgroundImage = (boolVisible)=> {
  return {
    type: TOGGLE_BACKGROUND_IMAGE,
    visible: boolVisible
  }
}

export const initialize = () => {
  return (dispatch) => {
    dispatch(fetchConfig())
    dispatch(fetchInfo())
    dispatch(fetchCounts())
    dispatch(loadData())
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
const applyNewQueryString = (newQueryString) => {
  if (newQueryString.indexOf('?') === 0) {
    newQueryString = newQueryString.slice(1)
  }
  const decodedQuery = decodeQueryString(newQueryString)
  const searchFromQuery = _.omit(_.get(decodedQuery, 'behavior.search'), 'selectedIds')
  const searchFromState = _.omit(_.get(store.getState(), 'behavior.search'), 'selectedIds')
  if (!_.isEqual(searchFromQuery, searchFromState)) {
    store.dispatch(clearCollections())
    store.dispatch(clearGranules())
    store.dispatch(updateSearch(searchFromQuery))
    store.dispatch(loadData())
  }
}

const queryWatch = watch(store.getState, 'behavior.routing.locationBeforeTransitions.search')
store.subscribe(queryWatch(applyNewQueryString))

// Update background
const updateBackground = (path) => {
  store.dispatch(toggleBackgroundImage(
    !(_.startsWith(path, '/508/') && path !== '/508/'
      || _.startsWith(path, '508/') && path !== '508/'))) //Cover strange routing case. TODO: Regex test?
}

const pathWatch = watch(store.getState, 'behavior.routing.locationBeforeTransitions.pathname')
store.subscribe(pathWatch(updateBackground))
