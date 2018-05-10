import React from 'react'
import {render} from 'react-dom'
import {Router, Route, IndexRoute} from 'react-router'
import Result from './result/Result'
import CollectionsContainer from './result/collections/CollectionsContainer'
import GranuleListContainer from './result/granules/GranuleListContainer'
import ErrorContainer from './error/ErrorContainer'
import LandingContainer from './landing/LandingContainer'
import DetailContainer from './detail/DetailContainer'
import Help from './common/info/Help'
import AboutContainer from './common/info/AboutContainer'
import {Provider} from 'react-redux'
import RootContainer from './root/RootContainer'
import {initialize} from './actions/FlowActions'
import '../style/style'
import './fonts.css'
import './page.css'
import './media.css'
import store from './store'
import history from './history'

store.dispatch(initialize())

const routesLayout = (
  <Router history={history}>
    <Route path="/" name="Home" component={RootContainer}>
      <IndexRoute component={LandingContainer} />
      <Route name="Collections" path="collections" component={Result}>
        <IndexRoute
          displayName="Collections"
          component={CollectionsContainer}
        />
      </Route>
      <Route
        name="Details"
        path="collections/details/:id"
        component={DetailContainer}
      />
      <Route
        name="GranuleDetail"
        path="collections/granules/:id"
        component={GranuleListContainer}
      />
      <Route name="Error" path="error" component={ErrorContainer} />
      <Route name="Help" path="help" component={Help} />
      <Route name="About" path="about" component={AboutContainer} />
    </Route>
  </Router>
)

const body = (
  <Provider store={store}>
    <div>{routesLayout}</div>
  </Provider>
)

const appDiv = document.createElement('div')
appDiv.setAttribute('id', 'app')
appDiv.setAttribute('style', 'height:100%')
document.body.appendChild(appDiv)

const fedAnalyticsScript = document.createElement('script')
fedAnalyticsScript.insertAdjacentHTML(
  'afterbegin',
  'window.ga=window.ga||function(){(ga.q=ga.q||[]).push(arguments)};ga.l=+new Date;' +
    "ga('create', 'UA-108560292-1', 'data.noaa.gov');" +
    "ga('set', 'anonymizeIp', true);" +
    "ga('send', 'pageview');"
)
document.body.appendChild(fedAnalyticsScript)

const googleAnalytics = document.createElement('script')
googleAnalytics.setAttribute(
  'src',
  'https://www.google-analytics.com/analytics.js'
)
googleAnalytics.setAttribute('type', 'text/javascript')
googleAnalytics.setAttribute('async', 'true')
document.body.appendChild(googleAnalytics)

const jsonLdScript = document.createElement('script')
jsonLdScript.setAttribute('type', 'application/ld+json')
jsonLdScript.insertAdjacentHTML(
  'afterbegin',
  `{
    "@context": "http://schema.org",
    "@type": "WebSite",
    "@id": "${window.location.origin + window.location.pathname}",
    "url": "${window.location.origin + window.location.pathname}",
    "potentialAction": {
      "@type": "SearchAction",
      "target": "${window.location.origin +
        window.location.pathname}/#/collections?q={search_term_string}",
      "query-input": "required name=search_term_string"
    }
  }`
)
document.body.appendChild(jsonLdScript)

render(body, appDiv)
