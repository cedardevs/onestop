import {connect} from 'react-redux'
import Header from './Header'
import {showHome} from '../actions/FlowActions'
import {withRouter} from 'react-router'
import {abbreviateNumber} from '../utils/readableUtils'
import {setHeaderMenuOpen} from '../actions/LayoutActions'

const mapStateToProps = state => {

  const numberOfGranulesSelected = Object.keys(state.cart.granules.selectedGranules).length
  const abbreviatedNumberOfGranulesSelected = abbreviateNumber(numberOfGranulesSelected)

  return {
    abbreviatedNumberOfGranulesSelected: abbreviatedNumberOfGranulesSelected
  }
}

const mapDispatchToProps = dispatch => {
  return {
    goHome: () => dispatch(showHome()),
    setHeaderMenuOpen: (isOpen) => dispatch(setHeaderMenuOpen(isOpen))
  }
}

const HeaderContainer = withRouter(
  connect(mapStateToProps, mapDispatchToProps)(Header)
)

export default HeaderContainer
