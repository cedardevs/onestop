import {connect} from 'react-redux'
import Cart from './Cart'

import {withRouter} from 'react-router'
import {
  removeAllGranulesFromLocalStorage,
  removeGranuleFromLocalStorage,
  getSelectedGranulesFromStorage,
} from '../../utils/localStorageUtil'
import {
  removeAllSelectedGranule,
  removeSelectedGranule,
} from '../../actions/cart/CartActions'

// import mockCartItems from '../../../test/cart/mockCartItems'

// const mapStateToProps = state => {
//   const numberOfGranulesSelected = Object.keys(
//     state.cart.granules.selectedGranules
//   ).length
//
//   return {
//     loading: state.ui.loading ? 1 : 0,
//     selectedGranules: state.cart.granules.selectedGranules,
//     numberOfGranulesSelected: numberOfGranulesSelected,
//   }
// }

const mapStateToProps = state => {
  const selectedGranules = getSelectedGranulesFromStorage(state)
  const numberOfGranulesSelected = selectedGranules
    ? Object.keys(getSelectedGranulesFromStorage(state)).length
    : 0

  // - these lines are ONLY for testing the cart
  // const selectedGranules = mockCartItems
  // const numberOfGranulesSelected = Object.keys(selectedGranules).length

  return {
    featuresEnabled: state.domain.config.featuresEnabled,
    loading: state.ui.loading ? 1 : 0,
    selectedGranules: selectedGranules,
    numberOfGranulesSelected: numberOfGranulesSelected,
  }
}

const mapDispatchToProps = dispatch => {
  return {
    deselectGranule: itemId => {
      removeGranuleFromLocalStorage(itemId)
      dispatch(removeSelectedGranule(itemId))
    },
    deselectAllGranules: () => {
      removeAllGranulesFromLocalStorage()
      dispatch(removeAllSelectedGranule())
    },
  }
}

const CartContainer = withRouter(
  connect(mapStateToProps, mapDispatchToProps)(Cart)
)

export default CartContainer
