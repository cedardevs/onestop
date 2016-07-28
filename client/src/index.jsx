import 'babel-polyfill'
import injectTapEventPlugin from 'react-tap-event-plugin'
import React from 'react'
import {render} from 'react-dom'
import { Router, Route, IndexRoute } from 'react-router'
import ResultsListContainer from './result/ResultsListContainer'
import LandingContainer from './landing/LandingContainer'
import {Provider} from 'react-redux'
import RootComponent from './components/Root.jsx'
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
