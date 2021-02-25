import {connect} from 'react-redux'
import Header from './Header'
import {showHome} from '../../actions//routing/HomeRouteActions'
import {withRouter} from 'react-router'
import {logoutUser} from '../../actions/UserActions'
import {FEATURE_CART} from '../../utils/featureUtils'

const mapStateToProps = state => {
  let authEnabled = false
  let loginEndpoint = undefined
  let logoutEndpoint = undefined
  let userProfileEndpoint = undefined

  if (state.config.auth) {
    authEnabled = !!state.config.auth
    loginEndpoint = state.config.auth.loginEndpoint
    logoutEndpoint = state.config.auth.logoutEndpoint
    userProfileEndpoint = state.config.auth.userProfileEndpoint
  }

  const user = state.user
  const dropdownAvailable = state.config.headerDropdownMenuFeatureAvailable
  const featuresEnabled = state.config.featuresEnabled

  const cartEnabled = featuresEnabled.includes(FEATURE_CART)

  return {
    headerDropdownMenuFeatureAvailable: dropdownAvailable,
    cartEnabled: cartEnabled,
    authEnabled: authEnabled,
    user: authEnabled && user ? user : null,
    loginEndpoint: authEnabled && loginEndpoint ? loginEndpoint : null,
    logoutEndpoint: authEnabled && logoutEndpoint ? logoutEndpoint : null,
    userProfileEndpoint:
      authEnabled && userProfileEndpoint ? userProfileEndpoint : null,
  }
}
const mapDispatchToProps = (dispatch, ownProps) => {
  return {
    goHome: () => dispatch(showHome(ownProps.history)),
    logoutUser: endpoint => dispatch(logoutUser(endpoint)),
  }
}

const HeaderContainer = withRouter(
  connect(mapStateToProps, mapDispatchToProps)(Header)
)

export default HeaderContainer
