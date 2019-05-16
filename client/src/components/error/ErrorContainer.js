import {connect} from 'react-redux'
import {withRouter} from 'react-router'
import {goBack} from 'connected-react-router'
import Error from './Error'
import {clearErrors} from '../../actions/ErrorActions'
import {collectionUpdateFilters} from '../../actions/search/CollectionFilterActions'
import {showHome} from '../../actions/search/SearchActions'

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
      dispatch(collectionUpdateFilters())
      dispatch(showHome(ownProps.history))
    },
  }
}

const ErrorContainer = withRouter(
  connect(mapStateToProps, mapDispatchToProps)(Error)
)

export default ErrorContainer
