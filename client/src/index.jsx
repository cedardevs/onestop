import React from 'react'
import {render} from 'react-dom'
import App from './App'
import store from './store' // create Redux store with appropriate middleware
import history from './history' // create history object based on environment

import '../img/noaa-favicon.ico'
import './fonts' // include custom fonts in bundle
import './style/fonts.css' // map custom fonts to CSS @font-face rules
import './style/vendorStyles' // vendor CSS rules (e.g. - leaflet)
import './style/page.css' // high-level and global CSS rules (must be before media.css)
import './style/media.css' // @media CSS rules for base font-size based on screen width
import './leaflet-init' // custom leaflet hooks
import './init' // dispatch initial conditions on page refresh
import {getBasePath} from './utils/urlUtils'
// create root DOM element for application
const appDiv = document.createElement('div')
appDiv.id = 'app'
document.body.appendChild(appDiv)

// specify the base URL for all relative URLs in the page
const baseRef = document.createElement('base')
baseRef.href = getBasePath()
document.head.appendChild(baseRef)

// render the app
render(App(store, history), appDiv)
