import {createStore, applyMiddleware, compose} from 'redux'
import {routerMiddleware} from 'react-router-redux'
// import {hashHistory} from 'react-router'
import Immutable from 'seamless-immutable'
import thunk from 'redux-thunk'
import reducer from './reducers/reducer'

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
  Immutable(),
  composeEnhancers(applyMiddleware(thunk))
  //composeEnhancers(applyMiddleware(thunk, routerMiddleware(hashHistory)))
)

export default store
