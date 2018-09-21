import {connect} from 'react-redux'
import Cart from './Cart'

import {withRouter} from 'react-router'

const mapStateToProps = state => {
  const numberOfGranulesSelected = Object.keys(
    state.cart.granules.selectedGranules
  ).length

  return {
    loading: state.ui.loading ? 1 : 0,
    selectedGranules: state.cart.granules.selectedGranules,
    numberOfGranulesSelected: numberOfGranulesSelected,
  }
}

const mapDispatchToProps = dispatch => {
  return {}
}

const CartContainer = withRouter(
  connect(mapStateToProps, mapDispatchToProps)(Cart)
)

export default CartContainer
