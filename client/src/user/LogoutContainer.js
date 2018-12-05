import {connect} from 'react-redux'
import Logout from './Logout'

import {withRouter} from 'react-router'

import {logoutUser} from '../actions/UserActions'

const mapStateToProps = state => {
  return {
    user: state.domain.user,
  }
}

const mapDispatchToProps = dispatch => {
  return {
    logoutUser: () => dispatch(logoutUser()),
  }
}

const LogoutContainer = withRouter(
  connect(mapStateToProps, mapDispatchToProps)(Logout)
)

export default LogoutContainer
