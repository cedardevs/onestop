import 'babel-polyfill'
import injectTapEventPlugin from 'react-tap-event-plugin'
import React from 'react'
import Immutable from 'immutable'
import {render} from 'react-dom'
import { Router, Route, IndexRoute, hashHistory } from 'react-router'
import { syncHistoryWithStore, routerMiddleware } from 'react-router-redux'
import {createStore, applyMiddleware} from 'redux'
import ResultsListContainer from './result/ResultsListContainer'
import LandingContainer from './landing/LandingContainer'
import thunk from 'redux-thunk'
import {Provider} from 'react-redux'
import RootComponent from './components/Root.jsx'
import reducer from './reducers/main'
import '../style/style.js'
import './page.css'
import store from './store.jsx'
import history from './history.jsx'

// Needed for onTouchTap
// Can go away when react 1.0 release
// Check this repo:
// https://github.com/zilverline/react-tap-event-plugin
injectTapEventPlugin()

const body =
    <Provider store={store}>
      <Router history={history}>
        <Route path="/" name="Home" component={RootComponent}>
          <IndexRoute component={LandingContainer}/>
          <Route name="Results" path="results" component={ResultsListContainer}/>
        </Route>
      </Router>
    </Provider>

var appDiv = document.createElement('div')
appDiv.setAttribute('id', 'app')
document.body.appendChild(appDiv)

render(body, appDiv)
