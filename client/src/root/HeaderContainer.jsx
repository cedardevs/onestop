import { connect } from 'react-redux'
import HeaderComponent from './HeaderComponent'
import { showHome } from '../actions/FlowActions'

const mapStateToProps = (state) => {
  return {}
}

const mapDispatchToProps = (dispatch) => {
  return {
    goHome: () => dispatch(showHome())
  }
}

const HeaderContainer = connect(
    mapStateToProps,
    mapDispatchToProps
)(HeaderComponent)

export default HeaderContainer
