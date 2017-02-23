import _ from 'lodash'
import watch from 'redux-watch'
import { push } from 'react-router-redux'
import { encodeQueryString, decodeQueryString } from '../utils/queryUtils'
import { triggerSearch, fetchGranules } from './SearchRequestActions'
import { updateSearch } from './SearchParamActions'
import { fetchConfig } from './ConfigActions'
import store from '../store'

export const showCollections = () => {
  return (dispatch, getState) => {
    dispatch(push(`collections?${encodeQueryString(getState())}`))
  }
}

export const showGranules = () => {
  return (dispatch, getState) => {
    dispatch(push(`collections/files?${encodeQueryString(getState())}`))
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

export const initialize = () => {
  return (dispatch) => {
    dispatch(fetchConfig())
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
    store.dispatch(updateSearch(searchFromQuery))
    store.dispatch(loadData())
  }
}

const w = watch(store.getState, 'behavior.routing.locationBeforeTransitions.search')
store.subscribe(w(applyNewQueryString))
