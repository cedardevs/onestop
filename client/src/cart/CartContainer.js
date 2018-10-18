import {connect} from 'react-redux'
import Cart from './Cart'

import {withRouter} from 'react-router'
import {removeGranule, getSelectedGranulesFromStorage} from '../utils/localStorageUtil'
// import {decrementSelectedGranuleCount} from '../../actions/CartActions'

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
  const selectedGranules = getSelectedGranulesFromStorage()
  const numberOfGranulesSelected = selectedGranules ?  Object.keys(selectedGranules).length : 0

  return {
    loading: state.ui.loading ? 1 : 0,
    selectedGranules: selectedGranules,
    numberOfGranulesSelected: numberOfGranulesSelected,
  }
}

const mapDispatchToProps = dispatch => {
  // return {
  //   deselectGranule: (item, itemId) => {
  //     removeGranule(item, itemId)
  //     dispatch(decrementSelectedGranuleCount())
  //   }
  // }
  return {}
}

const CartContainer = withRouter(
  connect(mapStateToProps, mapDispatchToProps)(Cart)
)

export default CartContainer
