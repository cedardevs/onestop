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
  return {
    user: state.user,
    savedSearches: state.user.searches,
    savedSearchEndpoint: state.config.auth
      ? state.config.auth.savedSearchEndpoint
      : null,
  }
}

const mapDispatchToProps = (dispatch, ownProps) => {
  return {
    deleteSearch: (savedSearchEndpoint, id) => {
      dispatch(deleteSearch(savedSearchEndpoint, id))
    },
    getSavedSearches: savedSearchEndpoint => {
      dispatch(getSavedSearches(savedSearchEndpoint))
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
