import store from '../store'
import Immutable from 'seamless-immutable'
import {triggerSearch} from '../actions/SearchRequestActions'
import {fetchGranules} from '../actions/SearchRequestActions'

const {baseUrl, encodedState} = parseUrl()

export const executeSearch = function(){
  store.dispatch(triggerSearch())
  // Add granule search (if needed) when collection search finishes
  if (baseUrl.endsWith('files')) {
    const checkInitialize = () => store.getState().routing.initialized
    const subscribeInitialized = () => {
      if (checkInitialize()) {
        unsubscribe()
        dispatch(fetchGranules())
      }
    }
    let unsubscribe = store.subscribe(subscribeInitialized)
  }
}

export const initialState = function(){
  if (encodedState) {
    return Immutable({behavior: {search: encodedState}})
  }
  else {
    return Immutable({})
  }
}

function parseUrl(){
  if (typeof document !== 'undefined') {
    const urlString = decodeURIComponent(document.location.hash)
    if (urlString.includes('?')) {
      const urlArray = urlString.split('?')
      return {baseUrl: urlArray[0], encodedState: JSON.parse(urlArray[1])}
    }
    else {
      return {baseUrl: document.URL, encodedState: ''}
    }
  }
  else {
    return {}
  }
}
