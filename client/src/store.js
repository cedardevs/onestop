import {createStore, applyMiddleware} from 'redux'
import { routerMiddleware } from 'react-router-redux'
import { hashHistory } from 'react-router'
import Immutable from 'seamless-immutable'
import thunk from 'redux-thunk'
import reducer from './reducers/reducer'
import _ from 'lodash'

const store = createStore(reducer, initialState(),
    applyMiddleware(
        thunk,
        routerMiddleware(hashHistory)
    ))

function initialState() {
  const initState = searchAndFacetState()
  if (!_.isEmpty(initState)){
    return Immutable({searchAndFacets: initState})
  } else {
    return Immutable({})
  }
}

function searchAndFacetState() {
  if(typeof document !== "undefined"){
    const urlString = decodeURIComponent(document.location.hash)
    if (urlString.includes('?')){
      const urlArray = urlString.split('?')
      return JSON.parse(urlArray[1])
    } else {
      return {}
    }
  } else {
    return {}
  }
}

export default store
