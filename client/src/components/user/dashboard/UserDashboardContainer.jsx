import {connect} from 'react-redux'
import UserDashboard from './UserDashboard'
import {withRouter} from 'react-router'

import {submitCollectionSearchWithFilter} from "../../../actions/routing/CollectionSearchRouteActions"
import history from '../../../history'

const mapStateToProps = state => {
  console.log('UserDashboardContainer :: mapStateToProps')
  console.log(state)

  return {
    user: state.user.profile,
    savedSearches: state.user.searches,
  }
}

const mapDispatchToProps = (dispatch, ownProps) => {
  return {
    navigateToSearch: (filter) => {
      dispatch(submitCollectionSearchWithFilter(history, filter))
    },
  }
}
const UserDashboardContainer = withRouter(
  connect(mapStateToProps, mapDispatchToProps)(UserDashboard)
)

export default UserDashboardContainer
