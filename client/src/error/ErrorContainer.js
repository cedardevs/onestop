import {connect} from 'react-redux'
import Error from './Error'
import {push, goBack} from 'react-router-redux'
import {clearErrors} from '../actions/ErrorActions'
import {updateSearch} from '../actions/SearchParamActions'

const mapStateToProps = state => {
  return {
    errors: state.behavior.errors,
  }
}

const mapDispatchToProps = dispatch => {
  return {
    goBack: () => {
      dispatch(clearErrors())
      dispatch(goBack())
    },
    goHome: () => {
      dispatch(clearErrors())
      dispatch(updateSearch())
      dispatch(push('/'))
    },
  }
}

const ErrorContainer = connect(mapStateToProps, mapDispatchToProps)(Error)

export default ErrorContainer
