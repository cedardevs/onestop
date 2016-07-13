import 'babel-polyfill'
import injectTapEventPlugin from 'react-tap-event-plugin'
import React from 'react'
import {render} from 'react-dom'
import { Router, Route, browserHistory, hashHistory, IndexRoute } from 'react-router'
import { syncHistoryWithStore } from 'react-router-redux'
import {createStore, applyMiddleware} from 'redux'
import ResultsContainer from './result/ResultContainer'
import LandingComponent from './search/LandingComponent'
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

let store = createStore(reducer, applyMiddleware(thunk))

// Create enhanced history object for router
const createSelectLocationState = () => {
  let prevRoutingState, prevRoutingStateJS
  return (state) => {
    const routingState = state.get('routing')
    if (typeof prevRoutingState === 'undefined' || prevRoutingState !== routingState) {
      prevRoutingState = routingState
      prevRoutingStateJS = routingState.toJS()
    }
    return prevRoutingStateJS
  }
}

const history = syncHistoryWithStore(hashHistory, store, {
  selectLocationState: createSelectLocationState()
})

const body =
    <Provider store={store}>
      <Router history={history}>
        <Route path="/" component={Root}>
          <IndexRoute component={LandingComponent}/>
        </Route>
      </Router>
    </Provider>

var appDiv = document.createElement('div')
appDiv.setAttribute('id', 'app')
document.body.appendChild(appDiv)

render(body, appDiv)
