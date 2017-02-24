import { connect } from 'react-redux'
import HeaderComponent from './HeaderComponent'

const mapStateToProps = (state) => {
  return {}
}

const mapDispatchToProps = (dispatch) => {
  return {}
}

const HeaderContainer = connect(
    mapStateToProps,
    mapDispatchToProps
)(HeaderComponent)

export default HeaderContainer
