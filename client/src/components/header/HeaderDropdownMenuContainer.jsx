import {connect} from 'react-redux'
import HeaderDropdownMenu from './HeaderDropdownMenu'
import {withRouter} from 'react-router'
import {abbreviateNumber} from '../../utils/readableUtils'
import {setHeaderMenuOpen} from '../../actions/LayoutActions'

const mapStateToProps = state => {
  const selectedGranules = state.cart.selectedGranules
  const numberOfGranulesSelected = Object.keys(selectedGranules).length
  const abbreviatedNumberOfGranulesSelected = abbreviateNumber(
    numberOfGranulesSelected
  )
  return {
    open: state.layout.headerMenuOpen,
    featuresEnabled: state.config.featuresEnabled,
    abbreviatedNumberOfGranulesSelected: abbreviatedNumberOfGranulesSelected,
  }
}

const mapDispatchToProps = dispatch => {
  return {
    setOpen: isOpen => dispatch(setHeaderMenuOpen(isOpen)),
  }
}

const HeaderDropdownMenuContainer = withRouter(
  connect(mapStateToProps, mapDispatchToProps)(HeaderDropdownMenu)
)

export default HeaderDropdownMenuContainer
