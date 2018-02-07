import {createStore, applyMiddleware, compose} from 'redux'
import {routerMiddleware} from 'react-router-redux'
import {hashHistory} from 'react-router'
import Immutable from 'seamless-immutable'
import thunk from 'redux-thunk'
import reducer from './reducers/reducer'
import {decodeLocation} from './utils/queryUtils'

const location = typeof document !== 'undefined' ? document.location.hash : ''
const initialState = Immutable(decodeLocation(location))

const getCompose = () => {
  if (
    typeof window === 'object' &&
    window.__REDUX_DEVTOOLS_EXTENSION_COMPOSE__
  ) {
    return window.__REDUX_DEVTOOLS_EXTENSION_COMPOSE__
  }
  return compose
}

const composeEnhancers = getCompose()
const store = createStore(
  reducer,
  initialState,
  composeEnhancers(applyMiddleware(thunk, routerMiddleware(hashHistory)))
)

export default store
