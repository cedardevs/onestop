import {connect} from 'react-redux'
import Header from './Header'
import {showHome} from '../actions/FlowActions'
import {withRouter} from 'react-router'
import {getUser} from '../actions/UserActions'

const mapStateToProps = state => {
  const authEnabled = !!state.domain.config.auth
  return {
    headerDropdownMenuFeatureAvailable:
      state.domain.config.headerDropdownMenuFeatureAvailable,
    authEnabled: authEnabled,
    user: authEnabled && state.domain.user ? state.domain.user : null,
    loginEndpoint: authEnabled
      ? state.domain.config.auth.loginEndpoint
        ? state.domain.config.auth.loginEndpoint
        : null
      : null,
    logoutEndpoint: authEnabled
      ? state.domain.config.auth.logoutEndpoint
        ? state.domain.config.auth.logoutEndpoint
        : null
      : null,
    userProfileEndpoint: authEnabled
      ? state.domain.config.auth.userProfileEndpoint
        ? state.domain.config.auth.userProfileEndpoint
        : null
      : null,
  }
}

const mapDispatchToProps = (dispatch, ownProps) => {
  return {
    goHome: () => dispatch(showHome(ownProps.history)),
    getUser: userProfileEndpoint => dispatch(getUser(userProfileEndpoint)),
  }
}

const HeaderContainer = withRouter(
  connect(mapStateToProps, mapDispatchToProps)(Header)
)

export default HeaderContainer
