import React from 'react'
import {render} from 'react-dom'
import { Router, Route, IndexRoute } from 'react-router'
import ResultContainer from './result/ResultContainer'
import Section508ResultLayout from './result/Section508ResultLayout'
import CollectionGridContainer from './result/collections/CollectionGridContainer'
import GranuleListContainer from './result/granules/list/GranuleListContainer'
import Section508CollectionGridContainer from './result/collections/Section508CollectionGridContainer'
import Section508GranuleListContainer from './result/granules/list/Section508GranuleListContainer'
import ErrorContainer from './error/ErrorContainer'
import LandingContainer from './landing/LandingContainer'
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
      <Route name="Collections" path="collections" component={ResultContainer}>
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
    <div>
      {routesLayout}
    </div>
  </Provider>

const appDiv = document.createElement('div')
appDiv.setAttribute('id', 'app')
appDiv.setAttribute('style', 'height:100%')
document.body.appendChild(appDiv)

const fedAnalyticsScript = document.createElement('script')
fedAnalyticsScript.insertAdjacentHTML('afterbegin',
  'window.ga=window.ga||function(){(ga.q=ga.q||[]).push(arguments)};ga.l=+new Date;' +
  'ga(\'create\', \'UA-108560292-1\', \'data.noaa.gov\');' +
  'ga(\'set\', \'anonymizeIp\', true);' +
  'ga(\'send\', \'pageview\');')
document.body.appendChild(fedAnalyticsScript)

const googleAnalytics = document.createElement('script')
googleAnalytics.setAttribute('src', 'https://www.google-analytics.com/analytics.js')
googleAnalytics.setAttribute('type', 'text/javascript')
googleAnalytics.setAttribute('async', 'true')
document.body.appendChild(googleAnalytics)

render(body, appDiv)