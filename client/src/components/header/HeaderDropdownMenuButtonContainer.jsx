import {connect} from 'react-redux'
import {withRouter} from 'react-router'
import {setHeaderMenuOpen} from '../../actions/LayoutActions'
import HeaderDropdownMenuButton from './HeaderDropdownMenuButton'

const mapStateToProps = state => {
  return {
    open: state.layout.headerMenuOpen,
  }
}

const mapDispatchToProps = dispatch => {
  return {
    setOpen: isOpen => dispatch(setHeaderMenuOpen(isOpen)),
  }
}

const HeaderDropdownMenuButtonContainer = withRouter(
  connect(mapStateToProps, mapDispatchToProps)(HeaderDropdownMenuButton)
)

export default HeaderDropdownMenuButtonContainer
