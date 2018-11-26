import {connect} from 'react-redux'
import Login from './Login'

import {withRouter} from 'react-router'

import {getUser} from '../actions/UserActions'

const mapStateToProps = state => {
    return {
        user: state.domain.user,
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
