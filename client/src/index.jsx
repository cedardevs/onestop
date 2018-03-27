import React from 'react'
import {render} from 'react-dom'
import {Router, Route, IndexRoute} from 'react-router'
import Result from './result/Result'
import Section508Result from './result/Section508Result'
import CollectionsContainer from './result/collections/CollectionsContainer'
import GranuleListContainer from './result/granules/GranuleListContainer'
import Section508CollectionGridContainer from './result/collections/Section508CollectionGridContainer'
import Section508GranuleListContainer from './result/granules/list/Section508GranuleListContainer'
import ErrorContainer from './error/ErrorContainer'
import LandingContainer from './landing/LandingContainer'
import Section508LandingContainer from './landing/Section508LandingContainer'
import DetailContainer from './detail/DetailContainer'
import Help from './common/info/Help'
import AboutContainer from './common/info/AboutContainer'
import {Provider} from 'react-redux'
import RootContainer from './root/RootContainer'
import {initialize} from './actions/FlowActions'
import '../style/style'
import './page.css'
import store from './store'
import history from './history'

store.dispatch(initialize())

// const routesDefinition = {
//   path: '/',
//   component: RootContainer,
//   indexRoute: {component: LandingContainer},
//   childRoutes: [
//     {
//       component: Result,
//       childRoutes: [
//         {
//           path: 'collections',
//           component: CollectionGridContainer,
//         },
//         {path: 'collections/details/:id', component: DetailContainer},
//         {path: 'collections/granules/:id', component: GranuleListContainer},
//       ],
//     },
//     {path: 'error', component: ErrorContainer},
//     {path: 'help', component: Help},
//     {path: 'about', component: AboutContainer},
//     {
//       path: '508',
//       component: Section508LandingContainer,
//     },
//     {
//       component: Section508Result,
//       childRoutes: [
//         {
//           path: '508/collections',
//           component: Section508CollectionGridContainer,
//         },
//         {
//           path: '508/collections/files',
//           component: Section508GranuleListContainer,
//         },
//       ],
//     },
//
//     {path: '508/error', component: ErrorContainer},
//     {path: '508/help', component: Help},
//     {path: '508/about', component: AboutContainer},
//   ],
// }
//
// const body = (
//     <Provider store={store}>
//       <Router history={history} routes={routesDefinition} />
//     </Provider>
// )

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

    <Route path="508" name="Home" component={RootContainer}>
      <IndexRoute component={Section508LandingContainer} />
      <Route name="Collections" path="collections" component={Section508Result}>
        <IndexRoute
          displayName="Collections"
          component={Section508CollectionGridContainer}
        />
        <Route
          name="Files"
          path="files"
          component={Section508GranuleListContainer}
        />
      </Route>
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

render(body, appDiv)
