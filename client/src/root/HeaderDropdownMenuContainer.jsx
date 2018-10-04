import {connect} from 'react-redux'
import HeaderDropdownMenu from './HeaderDropdownMenu'
import {withRouter} from 'react-router'
import {abbreviateNumber} from '../utils/readableUtils'

const mapStateToProps = state => {
  const numberOfGranulesSelected = Object.keys(
    state.cart.granules.selectedGranules
  ).length
  const abbreviatedNumberOfGranulesSelected = abbreviateNumber(
    numberOfGranulesSelected
  )

  return {
    open: state.ui.layout.headerMenuOpen,
    cartEnabled: state.domain.config.shoppingCartEnabled,
    abbreviatedNumberOfGranulesSelected: abbreviatedNumberOfGranulesSelected,
  }
}

const mapDispatchToProps = dispatch => {
  return {}
}

const HeaderDropdownMenuContainer = withRouter(
  connect(mapStateToProps, mapDispatchToProps)(HeaderDropdownMenu)
)

export default HeaderDropdownMenuContainer
