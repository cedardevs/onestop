// utils/GoogleAnalytics.js
import React, {Component} from 'react'
import PropTypes from 'prop-types'
import ReactGA from 'react-ga'
import {Route} from 'react-router-dom'

export default class GoogleAnalytics extends Component {
  componentDidMount() {
    const {analyticsConfig, analyticsInitiated, initAnalytics} = this.props
    this.logPageChange(this.props.location.pathname, this.props.location.search)
  }

  componentDidUpdate({location: prevLocation}) {
    console.log('COMPONENT DID UPDATE')
    const {analyticsConfig, analyticsInitiated, initAnalytics} = this.props

    if (
      !analyticsInitiated &&
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
    const isDifferentSearch = search !== prevLocation.search

    if (analyticsInitiated && (isDifferentPathname || isDifferentSearch)) {
      console.log('SENDING PAGE CHANGE')
      this.logPageChange(pathname, search)
    }
  }

  logPageChange(pathname, search = '') {
    const page = pathname + search
    const {location} = window
    ReactGA.set({
      page,
      location: `${location.origin}${page}`,
      ...this.props.options,
    })
    ReactGA.pageview(page)
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
