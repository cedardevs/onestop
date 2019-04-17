import {connect} from 'react-redux'
import Error from './Error'
import {goBack} from 'connected-react-router'
import {clearErrors} from '../actions/ErrorActions'
import {updateSearch} from '../actions/SearchParamActions'
import {showHome} from '../actions/FlowActions'

import {withRouter} from 'react-router'

const mapStateToProps = state => {
  return {
    errors: state.behavior.errors,
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
      dispatch(updateSearch())
      dispatch(showHome(ownProps.history))
    },
  }
}

const ErrorContainer = withRouter(
  connect(mapStateToProps, mapDispatchToProps)(Error)
)

export default ErrorContainer
