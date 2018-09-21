import {connect} from 'react-redux'
import Header from './Header'
import {showHome} from '../actions/FlowActions'
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
    abbreviatedNumberOfGranulesSelected: abbreviatedNumberOfGranulesSelected,
    shoppingCartEnabled: state.domain.config.shoppingCartEnabled,
  }
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
