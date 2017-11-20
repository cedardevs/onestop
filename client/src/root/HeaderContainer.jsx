import { connect } from 'react-redux'
import Header from './Header'
import { showHome } from '../actions/FlowActions'

const mapStateToProps = state => {
  return {}
}

const mapDispatchToProps = dispatch => {
  return {
    goHome: () => dispatch(showHome()),
  }
}

const HeaderContainer = connect(mapStateToProps, mapDispatchToProps)(Header)

export default HeaderContainer
