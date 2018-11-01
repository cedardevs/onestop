import {createStore, applyMiddleware, compose} from 'redux'
import {connectRouter, routerMiddleware} from 'connected-react-router'
import Immutable from 'seamless-immutable'
import thunk from 'redux-thunk'
import history from './history'
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
  connectRouter(history)(reducer), // new root reducer with router state
  Immutable(),
  composeEnhancers(
    applyMiddleware(
      routerMiddleware(history), // for dispatching history actions
      thunk
    )
  )
)

export default store
