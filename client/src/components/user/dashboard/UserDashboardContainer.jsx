import {connect} from 'react-redux'
import UserDashboard from './UserDashboard'
import {withRouter} from 'react-router'

const mapStateToProps = state => {
  console.log('UserDashboardContainer :: mapStateToProps')
  console.log(state)

  return {
    user: state.user.profile,
    savedSearches: state.user.searches,
  }
}

const UserDashboardContainer = withRouter(
  connect(mapStateToProps)(UserDashboard)
)

export default UserDashboardContainer
