import 'babel-polyfill'
import React from 'react'
import {render} from 'react-dom'
import { Router, Route, IndexRoute } from 'react-router'
import ResultLayout from './result/ResultLayout'
import Section508ResultLayout from './result/Section508ResultLayout'
import CollectionGridContainer from './result/collections/CollectionGridContainer'
import GranuleListContainer from './result/granules/list/GranuleListContainer'
import Section508CollectionGridContainer from './result/collections/Section508CollectionGridContainer'
import Section508GranuleListContainer from './result/granules/list/Section508GranuleListContainer'
import ErrorContainer from './error/ErrorContainer'
import LandingContainer from './landing/LandingContainer'
import Background from './landing/background/BackgroundContainer'
import Section508LandingContainer from './landing/Section508LandingContainer'
import {Provider} from 'react-redux'
import RootComponent from './root/Root'
import { initialize } from './actions/FlowActions'
import '../style/style'
import styles from './page.css'
import store from './store'
import history from './history'

store.dispatch(initialize())

const routesLayout =
  <Router history={history}>
    <Route path="/" name="Home" component={RootComponent}>
      <IndexRoute component={LandingContainer}/>
      <Route name="Collections" path="collections" component={ResultLayout}>
        <IndexRoute displayName="Collections" component={CollectionGridContainer}/>
        <Route name="Files" path="files" component={GranuleListContainer}/>
      </Route>
      <Route name="Error" path="error" component={ErrorContainer}/>
    </Route>

    <Route path="508" name="Home" component={RootComponent}>
      <IndexRoute component={Section508LandingContainer}/>
      <Route name="Collections" path="collections" component={Section508ResultLayout}>
        <IndexRoute displayName="Collections" component={Section508CollectionGridContainer}/>
        <Route name="Files" path="files" component={Section508GranuleListContainer}/>
      </Route>
      <Route name="Error" path="error" component={ErrorContainer}/>
    </Route>
  </Router>

const body =
  <Provider store={store}>
    <Background pageData={routesLayout}>
    </Background>
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
