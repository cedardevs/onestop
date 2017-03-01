import { connect } from 'react-redux'
import HeaderComponent from './HeaderComponent'
import { showHome, toggleHelp, toggleAbout } from '../actions/FlowActions'

const mapStateToProps = (state) => {
  return {}
}

const mapDispatchToProps = (dispatch) => {
  return {
    goHome: () => dispatch(showHome()),
    toggleAbout: () => dispatch(toggleAbout()),
    toggleHelp: () => dispatch(toggleHelp())
  }
}

const HeaderContainer = connect(
    mapStateToProps,
    mapDispatchToProps
)(HeaderComponent)

export default HeaderContainer
