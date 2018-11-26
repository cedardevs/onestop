import {connect} from 'react-redux'
import Header from './Header'
import {showHome} from '../actions/FlowActions'
import {withRouter} from 'react-router'

const mapStateToProps = state => {
  return {
    headerDropdownMenuFeatureAvailable:
      state.domain.config.headerDropdownMenuFeatureAvailable,
    user: state.domain.user ? state.domain.user : null,
  }
}

const mapDispatchToProps = (dispatch, ownProps) => {
  return {
    goHome: () => dispatch(showHome(ownProps.history)),
  }
}

const HeaderContainer = withRouter(
  connect(mapStateToProps, mapDispatchToProps)(Header)
)

export default HeaderContainer
