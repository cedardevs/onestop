// utils/GoogleAnalytics.js
import React, { Component } from 'react'
import PropTypes from 'prop-types'
import ReactGA from 'react-ga'
import { Route } from 'react-router-dom'

export default class GoogleAnalytics extends Component {
    //
    // constructor(props) {
    //     console.log(props)
    //
    //     super(props)
    //     // this.props = props
    //     // const {analyticsConfig, analyticsInitiated, initAnalytics} = this.props
    //     // console.log(analyticsConfig)
    //     // console.log(analyticsInitiated)
    //     // console.log(initAnalytics)
    //
    //     // if(!analyticsInitiated){
    //     //     ReactGA.initialize(analyticsConfig.profiles, analyticsConfig.reactGaOptions)
    //     //     initAnalytics()
    //     // }
    //     this.props = props
    // }

    componentDidMount () {
        const {analyticsConfig, analyticsInitiated, initAnalytics} = this.props
        console.log("COMPONENT DID MOUNT")
        console.log(this.props)
        // console.log(analyticsConfig)
        // console.log(analyticsInitiated)
        // console.log(initAnalytics)
        this.logPageChange(
            this.props.location.pathname,
            this.props.location.search
        )
    }

    componentDidUpdate ({ location: prevLocation }) {
        const { location: { pathname, search } } = this.props
        const isDifferentPathname = pathname !== prevLocation.pathname
        const isDifferentSearch = search !== prevLocation.search

        if (isDifferentPathname || isDifferentSearch) {
            console.log("SENDING PAGE CHANGE")
            this.logPageChange(pathname, search)
        }
    }

    logPageChange (pathname, search = '') {
        const page = pathname + search
        const { location } = window
        ReactGA.set({
            page,
            location: `${location.origin}${page}`,
            ...this.props.options
        })
        ReactGA.pageview(page)
    }

    render () {
        return <Route path="/"/>
    }
}

GoogleAnalytics.propTypes = {
    analyticsConfig: PropTypes.shape({
        profiles: PropTypes.array,
        options: PropTypes.object
    }),
    location: PropTypes.shape({
        pathname: PropTypes.string,
        search: PropTypes.string
    }).isRequired,
    options: PropTypes.object
}

const RouteTracker = () =>
    <Route component={GoogleAnalytics} />

const init = (options = {}) => {
    const env = window._env_ || process.env || {}
    const isGAEnabled = true
    // const isGAEnabled = !!env.ONESTOP_GA_TRACKING_ID
    console.log("INIT GA")
    console.log(options)
    console.log(options.profiles)
    console.log(options.reactGaOptions)

    if (isGAEnabled && options.profiles) {
        // ReactGA.initialize(
        //     'UA-127993388-1', {
        //         debug: 'true',
        //         ...options
        //     }
        // )
        ReactGA.initialize(options.profiles, options.reactGaOptions)
    }

    return isGAEnabled
}

// export default {
//     GoogleAnalytics,
//     RouteTracker,
//     init
// }