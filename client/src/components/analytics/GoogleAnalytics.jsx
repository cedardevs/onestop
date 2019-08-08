// utils/GoogleAnalytics.js
import React from 'react'
import PropTypes from 'prop-types'
import * as ReactGA from 'react-ga'
import {Route} from 'react-router-dom'

export default class GoogleAnalytics extends React.Component {
  componentDidMount() {
    const {location} = this.props
    this.logPageChange(location.pathname, location.collectionFilter)
  }

  componentDidUpdate({location: prevLocation}) {
    const {analyticsConfig, analyticsInitiated, initAnalytics} = this.props

    if (
      !analyticsInitiated &&
      analyticsConfig &&
      analyticsConfig.profiles &&
      analyticsConfig.profiles
    ) {
      ReactGA.initialize(
        analyticsConfig.profiles,
        analyticsConfig.reactGaOptions
      )
      initAnalytics()
    }

    const {location: {pathname, search}} = this.props
    const isDifferentPathname = pathname !== prevLocation.pathname
    const isDifferentSearch = search !== prevLocation.collectionFilter

    if (analyticsInitiated && (isDifferentPathname || isDifferentSearch)) {
      let profileNames = []
      if (analyticsConfig.profiles) {
        analyticsConfig.profiles.forEach(profile => {
          if (profile.gaOptions && profile.gaOptions.name) {
            profileNames.push(profile.gaOptions.name)
          }
        })
      }
      this.logPageChange(pathname, search, profileNames)
    }
  }

  logPageChange(pathname, search = '', profileNames) {
    const page = pathname + search
    const {location} = window
    ReactGA.set(
      {
        page,
        location: `${location.origin}${page}`,
        ...this.props.options,
      },
      profileNames
    )
    ReactGA.pageview(page, profileNames)
  }

  render() {
    return <Route path="/" />
  }
}

GoogleAnalytics.propTypes = {
  analyticsConfig: PropTypes.object,
  analyticsInitiated: PropTypes.bool,
  location: PropTypes.shape({
    pathname: PropTypes.string,
    search: PropTypes.string,
  }).isRequired,
  options: PropTypes.object,
}
