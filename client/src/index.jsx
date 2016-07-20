import 'babel-polyfill'
import injectTapEventPlugin from 'react-tap-event-plugin'
import React from 'react'
import Immutable from 'immutable'
import {render} from 'react-dom'
import { Router, Route, IndexRoute, browserHistory } from 'react-router'
import { syncHistoryWithStore, routerMiddleware } from 'react-router-redux'
import {createStore, applyMiddleware} from 'redux'
import ResultsListContainer from './result/ResultsListContainer'
import LandingContainer from './search/LandingContainer'
import thunk from 'redux-thunk'
import {Provider} from 'react-redux'
import Root from './components/Root.jsx'
import reducer from './reducers/main'
import '../style/style.js'
import './page.css'

// Needed for onTouchTap
// Can go away when react 1.0 release
// Check this repo:
// https://github.com/zilverline/react-tap-event-plugin
injectTapEventPlugin()

const initialState = Immutable.Map()
const store = createStore(reducer, initialState,
    applyMiddleware(
        thunk,
        routerMiddleware(browserHistory)
    ))

// Create enhanced history object for router
const createSelectLocationState = () => {
  let prevRoutingState, prevRoutingStateJS
  return (state) => {
    const routingState = state.get('routing')
    if (typeof prevRoutingState === 'undefined' || prevRoutingState !== routingState) {
      prevRoutingStateJS = routingState.toJS()
    }
    return prevRoutingStateJS
  }
}

const history = syncHistoryWithStore(browserHistory, store, {
  selectLocationState: createSelectLocationState()
})

const body =
    <Provider store={store}>
      <Router history={history}>
        <Route path="/" name="Home" component={Root}>
          <IndexRoute component={LandingContainer}/>
          <Route name="Results" path="results" component={ResultsListContainer}/>
        </Route>
      </Router>
    </Provider>

var appDiv = document.createElement('div')
appDiv.setAttribute('id', 'app')
document.body.appendChild(appDiv)

render(body, appDiv)
