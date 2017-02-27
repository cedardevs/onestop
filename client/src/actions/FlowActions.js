import _ from 'lodash'
import { push } from 'react-router-redux'
import { encodeQueryString } from '../utils/queryUtils'
import { triggerSearch } from './SearchRequestActions'
import { fetchGranules } from './SearchRequestActions'
import { fetchConfig } from './ConfigActions'

export const initialize = () => {
  return (dispatch, getState) => {
    const state = getState()

    dispatch(fetchConfig())
    dispatch(triggerSearch())

    const collectionsSelected = !_.isEmpty(state.behavior.search.selectedIds)
    const granulesLoaded = !_.isEmpty(state.domain.results.granules)
    if (collectionsSelected && !granulesLoaded) {
      dispatch(fetchGranules())
    }
  }
}

export const showCollections = (prefix = '') => {
  return (dispatch, getState) => {
    let pathname = `${prefix}/collections`
    let locationDescriptor = {
      pathname,
      search: `?${encodeQueryString(getState())}`
    }
    dispatch(push(locationDescriptor))
  }
}

export const showGranules = (prefix = '') => {
  return (dispatch, getState) => {
    let pathname = `${prefix}/collections/files`
    let locationDescriptor = {
      pathname,
      search: `?${encodeQueryString(getState())}`
    }
    dispatch(push(locationDescriptor))
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
