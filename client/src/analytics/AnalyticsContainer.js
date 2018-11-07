import {connect} from 'react-redux'
import GoogleAnalytics from './GoogleAnalytics'

import {withRouter} from 'react-router'

import {initAnalytics} from '../actions/AnalyticsActions'

const mapStateToProps = state => {
    console.log(state.domain.config.googleAnalytics)
    return {
        analyticsConfig: state.domain.config.googleAnalytics,
        analyticsInitiated: state.domain.config.analyticsInitiated,
    }
}

const mapDispatchToProps = dispatch => {
    return {
        initAnalytics: () => {
            dispatch(initAnalytics())
        },
    }
}

const AnalyticsContainer = withRouter(
    connect(mapStateToProps, mapDispatchToProps)(GoogleAnalytics)
)

export default AnalyticsContainer
