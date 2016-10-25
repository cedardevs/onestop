import { connect } from 'react-redux'
import ErrorComponent from './ErrorComponent'
import { push, goBack } from 'react-router-redux'
import { clearErrors } from './ErrorActions'

const mapStateToProps = (state) => {
  return {
    errors: state.get('errors')
  }
}

const mapDispatchToProps = (dispatch) => {
  return {
    goBack: () => {
      dispatch(clearErrors())
      dispatch(goBack())
    },
    goHome: () => {
      dispatch(clearErrors())
      dispatch(push('/'))
    }
  }
}

const ErrorContainer = connect(
    mapStateToProps,
    mapDispatchToProps
)(ErrorComponent)

export default ErrorContainer
