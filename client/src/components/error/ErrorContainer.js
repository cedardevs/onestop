import {connect} from 'react-redux'
import {withRouter} from 'react-router'
import {goBack} from 'connected-react-router'
import Error from './Error'
import {clearErrors} from '../../actions/ErrorActions'
import {showHome} from '../../actions//routing/HomeRouteActions'

const mapStateToProps = state => {
  return {
    errors: state.errors,
  }
}

const mapDispatchToProps = (dispatch, ownProps) => {
  return {
    goBack: () => {
      dispatch(clearErrors())
      dispatch(goBack())
    },
    goHome: () => {
      dispatch(clearErrors())
      dispatch(showHome(ownProps.history))
    },
  }
}

const ErrorContainer = withRouter(
  connect(mapStateToProps, mapDispatchToProps)(Error)
)

export default ErrorContainer
