import {connect} from 'react-redux'
import Login from './Login'

import {withRouter} from 'react-router'

import {getUser} from '../actions/UserActions'

const mapStateToProps = state => {
  console.log(state.domain.config)
  return {
    user: state.domain.user,
    loginEndpoint: state.domain.config.auth.loginEndpoint,
  }
}

const mapDispatchToProps = dispatch => {
  return {
    userLogin: () => dispatch(getUser()),
  }
}

const LoginContainer = withRouter(
  connect(mapStateToProps, mapDispatchToProps)(Login)
)

export default LoginContainer
