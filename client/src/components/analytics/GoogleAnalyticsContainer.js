import {connect} from 'react-redux'
import {withRouter} from 'react-router'
import GoogleAnalytics from './GoogleAnalytics'
import {initAnalytics} from '../../actions/ConfigActions'

const mapStateToProps = state => {
  return {
    analyticsConfig: state.config.googleAnalytics,
    analyticsInitiated: state.config.analyticsInitiated,
  }
}

const mapDispatchToProps = dispatch => {
  return {
    initAnalytics: () => {
      dispatch(initAnalytics())
    },
  }
}

const GoogleAnalyticsContainer = withRouter(
  connect(mapStateToProps, mapDispatchToProps)(GoogleAnalytics)
)

export default GoogleAnalyticsContainer
