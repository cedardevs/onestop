import {connect} from 'react-redux'
import Header from './Header'
import {showHome} from '../actions/FlowActions'
import {withRouter} from 'react-router'

const mapStateToProps = state => {
  // TODO: get selected granules count to display on cart button
  return {}
}

const mapDispatchToProps = dispatch => {
  return {
    goHome: () => dispatch(showHome()),
  }
}

const HeaderContainer = withRouter(
  connect(mapStateToProps, mapDispatchToProps)(Header)
)

export default HeaderContainer
