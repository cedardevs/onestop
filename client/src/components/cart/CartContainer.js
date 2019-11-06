import {connect} from 'react-redux'
import {withRouter} from 'react-router'
import Cart from './Cart'
import {
  removeAllSelectedGranule,
  removeSelectedGranule,
} from '../../actions/CartActions'
import {submitCollectionDetail} from '../../actions/routing/CollectionDetailRouteActions'

// import mockCartItems from '../../../test/cart/mockCartItems'

const mapStateToProps = state => {
  const selectedGranules = state.cart.selectedGranules
  const numberOfGranulesSelected = selectedGranules
    ? Object.keys(selectedGranules).length
    : 0

  // - these lines are ONLY for testing the cart
  // const selectedGranules = mockCartItems
  // const numberOfGranulesSelected = Object.keys(selectedGranules).length

  return {
    featuresEnabled: state.config.featuresEnabled,
    selectedGranules: selectedGranules,
    numberOfGranulesSelected: numberOfGranulesSelected,
    collectionDetailFilter: state.search.collectionFilter, // just used to submit collection detail correctly
  }
}

const mapDispatchToProps = (dispatch, ownProps) => {
  return {
    deselectGranule: itemId => {
      dispatch(removeSelectedGranule(itemId))
    },
    deselectAllGranules: () => {
      dispatch(removeAllSelectedGranule())
    },
    selectCollection: (id, filterState) => {
      dispatch(submitCollectionDetail(ownProps.history, id, filterState))
    },
  }
}

const CartContainer = withRouter(
  connect(mapStateToProps, mapDispatchToProps)(Cart)
)

export default CartContainer
