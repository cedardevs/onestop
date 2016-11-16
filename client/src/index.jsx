import 'babel-polyfill'
import React from 'react'
import {render} from 'react-dom'
import { Router, Route, IndexRoute } from 'react-router'
import ResultLayout from './result/ResultLayout'
import CollectionGridContainer from './result/collections/CollectionGridContainer'
import GranuleListContainer from './result/granules/list/GranuleListContainer'
import ErrorContainer from './error/ErrorContainer'
import LandingContainer from './landing/LandingContainer'
import {Provider} from 'react-redux'
import RootComponent from './root/Root'
import loadQuery from './query'
import '../style/style'
import './page.css'
import store from './store'
import history from './history'

// If loading page with query params, resubmit search
loadQuery()

const body =
    <Provider store={store}>
      <Router history={history}>
        <Route path="/" name="Home" component={RootComponent}>
          <IndexRoute component={LandingContainer}/>
          <Route name="Results" path="results" component={ResultLayout}>
            <IndexRoute displayName="Collections" component={CollectionGridContainer}/>
            <Route name="Granules" path="granules" component={GranuleListContainer}/>
          </Route>
          <Route name="Error" path="error" component={ErrorContainer}/>
        </Route>
      </Router>
    </Provider>

var appDiv = document.createElement('div')
appDiv.setAttribute('id', 'app')
appDiv.setAttribute('style', 'height:100%')
document.body.appendChild(appDiv)

render(body, appDiv)
