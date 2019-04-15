import {connect} from 'react-redux'
import Header from './Header'
import {showHome} from '../../actions/FlowActions'
import {withRouter} from 'react-router'
import {getUser, logoutUser} from '../../actions/UserActions'
import {FEATURE_CART} from '../../utils/featureUtils'

const mapStateToProps = state => {
  let authEnabled = false
  let loginEndpoint = undefined
  let logoutEndpoint = undefined
  let userProfileEndpoint = undefined

  if (state.domain.config.auth) {
    authEnabled = !!state.domain.config.auth
    loginEndpoint = state.domain.config.auth.loginEndpoint
    logoutEndpoint = state.domain.config.auth.logoutEndpoint
    userProfileEndpoint = state.domain.config.auth.userProfileEndpoint
  }

  const user = state.domain.user
  const dropdownAvailable =
    state.domain.config.headerDropdownMenuFeatureAvailable
  const featuresEnabled = state.domain.config.featuresEnabled

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
    getUser: userProfileEndpoint => dispatch(getUser(userProfileEndpoint)),
    logoutUser: () => dispatch(logoutUser()),
  }
}

const HeaderContainer = withRouter(
  connect(mapStateToProps, mapDispatchToProps)(Header)
)

export default HeaderContainer
