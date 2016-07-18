import fetch from 'isomorphic-fetch'
import { browserHistory} from 'react-router'


export const INDEX_CHANGE = 'index_change'
export const SEARCH = 'search'
export const SEARCH_COMPLETE = 'search_complete'

export const startSearch = (searchText) => {
  return {
    type: SEARCH,
    searchText
  }
}

export const completeSearch = (searchText, items) => {
  return {
    type: SEARCH_COMPLETE,
    searchText,
    items
  }
}

export const textSearch = (searchText) => {
  return (dispatch, getState) => {
    // if a search is already in flight, let the calling code know there's nothing to wait for
    var s = getState()
    if (getState().getIn(['search', 'inFlight']) === true) {
      return Promise.resolve()
    }
    dispatch(startSearch(searchText))

    const geometry = getState().getIn(['search', 'geometry']).toJS()
    const apiRoot = "/api/search"
    const fetchParams = {
      method: 'POST',
      headers: {
        'Accept': 'application/json',
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        queries: [
          {type: 'queryText', value: searchText}
        ],
        filters: [
          { type: 'geometry', geometry: geometry }
        ]
      })
    }

    return fetch(apiRoot, fetchParams)
        .then(response => response.json())
        .then(json => dispatch(completeSearch(searchText, assignResourcesToMap(json.data))))
        .then(browserHistory.push('results'))
  }
}

const assignResourcesToMap = (resourceList) => {
  var map = new Map()
  resourceList.forEach(resource => {
    map.set(resource.id, Object.assign({type: resource.type}, resource.attributes))
  })
  return map
}
