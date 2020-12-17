import {connect} from 'react-redux'
import UserDashboard from './UserDashboard'
import {withRouter} from 'react-router'

import {submitCollectionSearchWithFilter} from '../../../actions/routing/CollectionSearchRouteActions'
import {
  getSavedSearches,
  deleteSearch,
} from '../../../actions/SavedSearchActions'
import history from '../../../history'

const mapStateToProps = state => {
  const loginEndpoint = state.config.auth
    ? state.config.auth.loginEndpoint
    : null
  return {
    user: state.user,
    loginEndpoint: loginEndpoint,
    configIsFetching: state.config.isFetching,
    savedSearches: state.user.searches,
    savedSearchEndpoint: state.config.auth
      ? state.config.auth.savedSearchEndpoint
      : null,
  }
}

const mapDispatchToProps = (dispatch, ownProps) => {
  return {
    deleteSearch: id => {
      dispatch(deleteSearch(ownProps.savedSearchEndpoint, id))
    },
    getSavedSearches: () => {
      dispatch(getSavedSearches(ownProps.savedSearchEndpoint))
    },
    navigateToSearch: filter => {
      dispatch(submitCollectionSearchWithFilter(history, filter))
    },
  }
}
const UserDashboardContainer = withRouter(
  connect(mapStateToProps, mapDispatchToProps)(UserDashboard)
)

export default UserDashboardContainer
