import {connect} from 'react-redux'
import HeaderDropdownMenu from './HeaderDropdownMenu'
import {withRouter} from 'react-router'

const mapStateToProps = state => {
  return {
    open: state.ui.layout.headerMenuOpen,
  }
}

const mapDispatchToProps = dispatch => {
  return {}
}

const HeaderDropdownMenuContainer = withRouter(
  connect(mapStateToProps, mapDispatchToProps)(HeaderDropdownMenu)
)

export default HeaderDropdownMenuContainer
