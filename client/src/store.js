import {applyMiddleware, compose, createStore} from 'redux'
import {routerMiddleware} from 'connected-react-router'
import thunk from 'redux-thunk'
import history from './history'
import reducer from './reducer'
import {preloadState} from './stateManager'
import {cartMiddleware} from './cartMiddleware'

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
  reducer(history), // root reducer with router state
  preloadState(), // pre-loaded state
  composeEnhancers(
    applyMiddleware(
      routerMiddleware(history), // for dispatching history actions
      thunk,
      cartMiddleware
    )
  )
)

export default store
