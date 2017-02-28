import 'babel-polyfill'
import React from 'react'
import {render} from 'react-dom'
import { Router, Route, IndexRoute } from 'react-router'
import ResultLayout from './result/ResultLayout'
import CollectionGridContainer from './result/collections/CollectionGridContainer'
import GranuleListContainer from './result/granules/list/GranuleListContainer'
import ErrorContainer from './error/ErrorContainer'
import LandingContainer from './landing/LandingContainer'
import Section508LandingContainer from './landing/Section508LandingContainer'
import {Provider} from 'react-redux'
import RootComponent from './root/Root'
import { initialize } from './actions/FlowActions'
import '../style/style'
import './page.css'
import store from './store'
import history from './history'

store.dispatch(initialize())

const body =
  <Provider store={store}>
    <Router history={history}>
      <Route path="/" name="Home" component={RootComponent}>
        <IndexRoute component={LandingContainer}/>
        <Route name="Landing508" path="landing-508" component={Section508LandingContainer}/>
        <Route name="Collections" path="collections" component={ResultLayout}>
          <IndexRoute displayName="Collections" component={CollectionGridContainer}/>
          <Route name="Files" path="files" component={GranuleListContainer}/>
        </Route>
        <Route name="Error" path="error" component={ErrorContainer}/>
      </Route>
    </Router>
  </Provider>

const appDiv = document.createElement('div')
appDiv.setAttribute('id', 'app')
appDiv.setAttribute('style', 'height:100%')
document.body.appendChild(appDiv)

const srcDiv = document.createElement('script')
srcDiv.setAttribute('id', '_fed_an_ua_tag')
srcDiv.setAttribute('type', 'text/javascript')
srcDiv.setAttribute('src', '/scripts/federated-analytics.js?agency=DOC%26subagency=NOAA')
srcDiv.setAttribute('async', 'true')
document.body.appendChild(srcDiv)

render(body, appDiv)