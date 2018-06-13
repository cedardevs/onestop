import _ from 'lodash'
import {push} from 'connected-react-router'
import {
  encodeQueryString,
  decodeQueryString,
  decodeLocation,
} from '../utils/queryUtils'
import {
  triggerSearch,
  fetchGranules,
  clearCollections,
} from './SearchRequestActions'
import {
  updateSearch,
  clearSelections,
  toggleSelection,
} from './SearchParamActions'
import {fetchConfig} from './ConfigActions'
import {fetchInfo, fetchCounts} from './InfoActions'

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

export const showGranulesList = id => {
  if (!id) {
    return
  }
  return (dispatch, getState) => {
    const query = encodeQueryString(getState())
    const locationDescriptor = {
      pathname: `/collections/granules/${id}`,
      search: `?${query}`,
    }
    dispatch(push(locationDescriptor))
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
    dispatch(push(locationDescriptor))
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
