import {createStore, applyMiddleware} from 'redux'
import { routerMiddleware } from 'react-router-redux'
import { hashHistory } from 'react-router'
import Immutable from 'seamless-immutable'
import thunk from 'redux-thunk'
import reducer from './reducers/reducer'
import { decodeQueryString } from './utils/queryUtils'

let queryString = ''
if(typeof document !== "undefined") {
  queryString = document.location.hash.split('?')[1]
}
const initialState = Immutable(decodeQueryString(queryString))

const store = createStore(reducer, initialState,
  applyMiddleware(
    thunk,
    routerMiddleware(hashHistory)
  ))
  
export default store
