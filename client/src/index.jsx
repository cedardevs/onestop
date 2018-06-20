import React from 'react'
import {render} from 'react-dom'
import {Provider} from 'react-redux'
import RootContainer from './root/RootContainer'
import './init'
import '../style/style'
import './fonts/fonts.css'
import './page.css'
import './media.css'
import store from './store'
import history from './history'
import './leaflet-init'

// force webpack to include the fonts in the build/dist
import './fonts/Merriweather-Regular.ttf'
import './fonts/Merriweather-Bold.ttf'
import './fonts/Merriweather-Italic.ttf'
import './fonts/Merriweather-BoldIt.ttf'
import './fonts/Merriweather-Light.ttf'
import './fonts/Merriweather-LightIt.ttf'
import './fonts/SourceSansPro-Regular.ttf'
import './fonts/SourceSansPro-Italic.ttf'
import './fonts/SourceSansPro-Bold.ttf'
import './fonts/SourceSansPro-BoldItalic.ttf'
import './fonts/SourceSansPro-Light.ttf'
import './fonts/SourceSansPro-LightItalic.ttf'
import './fonts/SourceSansPro-SemiBold.ttf'
import './fonts/SourceSansPro-SemiBoldItalic.ttf'

import {Route} from 'react-router'
import {ConnectedRouter} from 'react-router-redux'

const body = (
  <Provider store={store}>
    <ConnectedRouter history={history}>
      <RootContainer />
    </ConnectedRouter>
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

const rootUrl = `${window.location.origin + window.location.pathname}`

const jsonLdScript = document.createElement('script')
jsonLdScript.setAttribute('type', 'application/ld+json')
jsonLdScript.insertAdjacentHTML(
  'afterbegin',
  `{
    "@context": "http://schema.org",
    "@type": "WebSite",
    "@id": "${rootUrl}",
    "url": "${rootUrl}",
    "potentialAction": {
      "@type": "SearchAction",
      "target": "${rootUrl}#/collections?q={search_term_string}",
      "query-input": "required name=search_term_string"
    },
    "publisher": {
      "@type": "Organization",
      "@id": "https://www.ncei.noaa.gov/",
      "name": "National Centers for Environmental Information (NCEI)",
      "logo": {
          "@type": "ImageObject",
          "url": "https://www.ncei.noaa.gov/sites/default/files/noaa_logo_circle_72x72.svg",
          "width": "72",
          "height": "72"
      }
    }
  }`
)
document.body.appendChild(jsonLdScript)

const ogUrlMetaTag = document.createElement('meta')
ogUrlMetaTag.setAttribute('property', 'og:url')
ogUrlMetaTag.setAttribute('content', `${rootUrl}`)
document.head.appendChild(ogUrlMetaTag)

render(body, appDiv)
